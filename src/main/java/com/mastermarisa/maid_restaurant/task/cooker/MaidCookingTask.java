package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.api.MaidWorkBlockTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import com.mastermarisa.maid_restaurant.uitls.manager.StateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class MaidCookingTask extends MaidWorkBlockTask {
    public MaidCookingTask(){
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT),20);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (BehaviorUtils.getType(maid) == TargetType.COOK_BLOCK.type) {
            PositionTracker tracker = BehaviorUtils.getTargetPos(maid).get();
            BlockPos pos = tracker.currentBlockPosition();
            Vec3 targetPos = tracker.currentPosition();

            if (!maid.isPassenger()) return false;
            if (BlockUsageManager.getUserCount(pos) > 0 && !BlockUsageManager.isUsing(pos,maid.getUUID())) return false;
            if (maid.distanceToSqr(targetPos) > Math.pow(2.5D,2.0D)) return false;
            if (StateManager.cookState(maid) != StateManager.CookState.COOK) return false;

            return RequestManager.getCurrentTask(maid).map(iCookTask -> iCookTask.isValidWorkBlock(level,maid,pos)).orElse(false);
        }
        return false;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level,maid,gameTime)) return;
        BlockPos pos = BehaviorUtils.getTargetPos(maid).get().currentBlockPosition();
        CookRequest request = RequestManager.peekCookRequest(maid).get();
        ICookTask iCookTask = RequestManager.getCurrentTask(maid).get();

        if (request.count <= 0) {
            RequestManager.popCookRequest(maid);
            RequestManager.postServeRequest(RequestManager.popServeRequest(maid).get());
            setNextTickCount(1);
            return;
        }

        iCookTask.cookTick(level,maid,pos,request);
    }
}
