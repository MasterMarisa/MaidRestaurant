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
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class MaidExecuteCookStepTask extends MaidTickRateTask {
    private final double closeEnoughDistSqr;

    public MaidExecuteCookStepTask(double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) {
            return false;
        }

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        if (!BlockUsageUtil.isUsing(pos, maid.getUUID())) {
            return false;
        }

        double distVertical = Math.abs(maid.getY() - pos.getY());
        if (distVertical > 4) {
            return false;
        }

        ICookCapability capability = node.getCapability();
        return capability != null && capability.isValidWorkBlock(level, pos);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        MaidRestaurant.LOGGER.debug("MaidExecuteCookStepTask - START");
        this.ticksRemain = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        if (ticksRemain > 0){
            return true;
        } else {
            return MemoryUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP) && checkExtraStartConditions(level, maid);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();

        if (gameTime % 10 == 0) {
            double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
            if (distHorizontal > closeEnoughDistSqr) {
                maid.getBrain()
                        .getMemory(ModEntities.STAND_POS.get())
                        .ifPresent(tracker -> {
                            BlockPos walkPos = tracker.currentBlockPosition();
                            WalkTarget target = new WalkTarget(walkPos, 0.4F, 0);
                            MemoryUtil.setIfAbsent(maid, MemoryModuleType.WALK_TARGET, target);
                        });
            }
            MemoryUtil.setIfAbsent(maid, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        }

        if (!shouldTick(level, maid, gameTime)) {
            return;
        }

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        ICookCapability capability = node.getCapability();
        CookResult result = capability.cookTick(level, maid, pos, node.getRecipeNode());

        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);
        if (result == CookResult.DONE) {
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidStoreDishTask.UID, 5);
            this.doStop(level, maid, gameTime);
        } else if (result == CookResult.INTERRUPTED) {
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
            this.doStop(level, maid, gameTime);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(t -> {
            BlockUsageUtil.remove(t.currentBlockPosition(), maid.getUUID());
        });

        if (MemoryUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP)) {
            MemoryUtil.removeTarget(maid);
        }

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node != null) {
            node.verifyAndUpdateState(level, maid);
            node.computeParentState();
        }
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected int getInterval(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node != null) {
            ICookCapability capability = node.getCapability();
            return capability != null ? capability.getTickInterval() : 0;
        }
        return 0;
    }
}
