package com.mastermarisa.maid_restaurant.maid.task.cook;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.config.RestaurantConfig;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;

public class MaidCookingTask extends MaidTickRateTask {
    public MaidCookingTask() {
        super(ImmutableMap.of(
                ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                ModEntities.CHAIR_POS.get(), MemoryStatus.VALUE_PRESENT
        ),20);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (check(level,maid)) return true;
        stop(level,maid,0);
        return false;
    }

    private boolean check(ServerLevel level, EntityMaid maid) {
        if (BehaviorUtils.getTargetType(maid) == 2) {
            PositionTracker tracker = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get();
            BlockPos pos = tracker.currentBlockPosition();
            Vec3 chair = maid.getBrain().getMemory(ModEntities.CHAIR_POS.get()).get().currentPosition();

            CookRequest request = (CookRequest) RequestManager.peek(maid,CookRequest.TYPE);
            if (request == null) return false;
            if (request.remain <= 0) {
                Arrays.stream(request.targets).forEach(l -> BlockUsageManager.removeUser(EncodeUtils.decode(l),maid.getUUID()));
                RequestManager.post(level, ServeRequest.from(level,maid.getUUID(),(CookRequest) RequestManager.pop(maid,CookRequest.TYPE)), ServeRequest.TYPE);
                Debug.Log("SERVE_SENT");
                return false;
            }

            if (RestaurantConfig.SIT_WHILE_COOKING() && !maid.isPassenger()) return false;
            if (BlockUsageManager.getUserCount(pos) > 0 && !BlockUsageManager.isUsing(pos,maid.getUUID())) return false;
            if (!RestaurantConfig.SIT_WHILE_COOKING() && maid.distanceToSqr(chair) > Math.pow(0.3D,2.0D)) return false;
            if (MaidStateManager.cookState(maid,level) != MaidStateManager.CookState.COOK) return false;
            ICookTask iCookTask = CookTasks.getTask(request.type);

            return iCookTask.isValidWorkBlock(level,maid,pos);
        }
        return false;
    }

    @Override
    protected boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        return maid.getBrain().hasMemoryValue(ModEntities.TARGET_POS.get()) &&
                maid.getBrain().hasMemoryValue(ModEntities.CHAIR_POS.get()) &&
                checkExtraStartConditions(level, maid);
    }

    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime){
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent((posTracker)-> {
            BlockUsageManager.addUser(posTracker.currentBlockPosition(),maid.getUUID());
            maid.getBrain().setMemory(ModEntities.CACHED_WORK_BLOCK.get(),posTracker);
        });
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid,CookRequest.TYPE));
        Arrays.stream(request.targets).forEach(l -> BlockUsageManager.addUser(EncodeUtils.decode(l),maid.getUUID()));
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level,maid,gameTime)) return;
        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get().currentBlockPosition();
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid,CookRequest.TYPE));
        ICookTask iCookTask = CookTasks.getTask(request.type);
        //BehaviorUtils.setLookTargetMemory(maid,pos.below());

        iCookTask.cookTick(level,maid,pos,request);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (BehaviorUtils.getTargetType(maid) == 2) {
            CookRequest request = (CookRequest) RequestManager.peek(maid,CookRequest.TYPE);
            if (request != null)
                Arrays.stream(request.targets).forEach(l -> BlockUsageManager.removeUser(EncodeUtils.decode(l),maid.getUUID()));
            BehaviorUtils.eraseTargetPos(maid);
            if (RestaurantConfig.SIT_WHILE_COOKING()) BehaviorUtils.stopRide(level,maid);
            maid.getBrain().eraseMemory(ModEntities.CHAIR_POS.get());
            CheckRateManager.setNextCheckTick(maid.getUUID() + MaidApproachCookBlockTask.UID,5);
            CheckRateManager.setNextCheckTick(maid.getUUID() + MaidGetFromStorageTask.UID,5);
        }
    }
}
