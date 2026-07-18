package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class MaidApproachWorkBlockTask extends MaidCheckRateTask {
    public static final String UID = "ApproachWorkBlock";

    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidApproachWorkBlockTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) {
            return false;
        }
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.READY);
        if (node == null) {
            return false;
        }
        return searchWorkBlock(level, maid, node);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidApproachWorkBlockTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.isTarget(maid, TargetType.APPROACH_WORK_BLOCK) &&
                maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
                        MaidUtils.distSqrHorizontal(maid, tracker.currentBlockPosition()) > Math.pow(closeEnoughDist, 2.0D)
                        && Math.abs(maid.getY() - tracker.currentBlockPosition().getY()) <= 4
                ).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (gameTime % 10 != 0) return;
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BehaviorUtils.setWalkAndLookTargetMemories(maid, tracker.currentBlockPosition(), tracker.currentBlockPosition(), movementSpeed, 0);
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BlockPos pos = tracker.currentBlockPosition();
            if (MaidUtils.distSqrHorizontal(maid, pos) <= Math.pow(closeEnoughDist, 2.0D)
                    && Math.abs(maid.getY() - pos.getY()) <= 4) {
                onReached(level, maid, pos);
            }
        });
        if (BehaviorUtils.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)) BehaviorUtils.eraseTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchWorkBlock(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        RecipeStep step = node.getRecipeNode().getCombineStep();
        if (step == null) {
            return false;
        }

        String capabilityUID = step.getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null) {
            return false;
        }

        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (zone == null) {
            return false;
        }

        BlockPos workBlock = capability.searchWorkBlock(level, zone, maid);
        if (workBlock != null) {
            BehaviorUtils.setTarget(maid, new BlockPosTracker(workBlock), TargetType.APPROACH_WORK_BLOCK);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, workBlock, workBlock, movementSpeed, 1);
            return true;
        }
        return false;
    }

    private void onReached(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.READY);
        if (node == null) {
            return;
        }

        RecipeStep step = node.getRecipeNode().getCombineStep();
        if (step == null) {
            return;
        }

        String capabilityUID = step.getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null || !capability.isValidWorkBlock(level, pos)) {
            return;
        }

        // 验证前置需求
        node.verifyAndUpdateState(level, maid);
        if (node.getState() != NodeState.READY) {
            return;
        }

        node.setState(NodeState.EXECUTING);
        BlockUsageUtils.add(pos, maid.getUUID());
        BehaviorUtils.setTarget(maid, new BlockPosTracker(pos), TargetType.EXECUTE_COOK_STEP);
    }
}
