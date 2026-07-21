package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtil;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidExecuteCookStepTask extends MaidTickRateTask {
    public MaidExecuteCookStepTask() {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) {
            return false;
        }

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        if (BlockUsageUtil.isUsed(pos) && !BlockUsageUtil.isUsing(pos, maid.getUUID())) {
            return false;
        }

        ICookCapability capability = node.getCapability();
        return capability != null && capability.isValidWorkBlock(level, pos);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        MaidRestaurant.LOGGER.debug("MaidExecuteCookStepTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        if (ticksRemain > 0){
            return true;
        } else {
            return BehaviorUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP) && checkExtraStartConditions(level, maid);
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level, maid, gameTime)) {
            return;
        }

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) {
            return;
        }

        ICookCapability capability = node.getCapability();
        if (capability == null) {
            return;
        }

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        CookResult result = capability.cookTick(level, maid, pos, node.getRecipeNode());
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos.above()));

        if (result == CookResult.DONE) {
            node.verifyAndUpdateState(level, maid);
            if (node.getParent() != null) {
                node.getParent().computeState();
            }
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidStoreDishTask.UID, 5);
        } else if (result == CookResult.INTERRUPTED) {
            node.verifyAndUpdateState(level, maid);
        }
        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BlockUsageUtil.remove(tracker.currentBlockPosition(), maid.getUUID());
        });
        if (BehaviorUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP)) {
            BehaviorUtil.eraseTarget(maid);
        }
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected int getInterval(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node != null) {
            ICookCapability capability = node.getCapability();
            if (capability != null) {
                return capability.getTickInterval();
            }
        }
        return 0;
    }
}
