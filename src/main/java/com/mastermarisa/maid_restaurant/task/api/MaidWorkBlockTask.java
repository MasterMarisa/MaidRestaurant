package com.mastermarisa.maid_restaurant.task.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.cooker.MaidSearchStorageTask;
import com.mastermarisa.maid_restaurant.task.cooker.MaidSearchWorkBlockTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import com.mastermarisa.maid_restaurant.uitls.manager.CheckRateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class MaidWorkBlockTask extends MaidTickRateTask {
    public MaidWorkBlockTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxTickRate){
        super(entryCondition,maxTickRate);
    }

    @Override
    protected boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        return maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get()) && checkExtraStartConditions(level, maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime){
        BehaviorUtils.getTargetPos(maid).ifPresent((posTracker)-> {
            BlockUsageManager.addUser(posTracker.currentBlockPosition(),maid.getUUID());
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (BehaviorUtils.getType(maid) == 0) {
            BlockPos pos = BehaviorUtils.getTargetPos(maid).get().currentBlockPosition();
            if (BlockUsageManager.isUsing(pos,maid.getUUID())) {
                BehaviorUtils.eraseTargetPos(maid);
            }
            BehaviorUtils.eraseChairPos(maid);
            BehaviorUtils.stopRide(level,maid);
            CheckRateManager.setNextCheckTick(maid.getUUID() + MaidSearchWorkBlockTask.UID,5);
            CheckRateManager.setNextCheckTick(maid.getUUID() + MaidSearchStorageTask.UID,5);
        }
    }
}
