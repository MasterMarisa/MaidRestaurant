package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.recipe.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.recipe.NodeState;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

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
        if (!super.checkExtraStartConditions(level, maid)) return false;
        ExecutionNode readyNode = ChefScheduler.findReadyNode(maid);
        if (readyNode == null) return false;
        return searchWorkBlock(level, maid, readyNode);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.isTarget(maid, TargetType.APPROACH_WORK_BLOCK) &&
                maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
                        tracker.currentBlockPosition().distSqr(maid.blockPosition()) > Math.pow(closeEnoughDist, 2.0D)
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
            if (pos.distSqr(maid.blockPosition()) <= Math.pow(closeEnoughDist, 2.0D)) {
                onReached(level, maid, pos);
            }
        });
        if (BehaviorUtils.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)) BehaviorUtils.eraseTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private boolean searchWorkBlock(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        assert node.getRecipeNode().getCombineStep() != null;
        String capabilityUID = node.getRecipeNode().getCombineStep().getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null) return false;

        RestaurantZone zone = RestaurantZone.getZone(maid);
        if (zone == null || !zone.isValid()) return false;

        BlockPos workBlock = capability.searchWorkBlock(level, zone, maid);
        if (workBlock != null) {
            BehaviorUtils.setTarget(maid, new BlockPosTracker(workBlock), TargetType.APPROACH_WORK_BLOCK);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, workBlock, workBlock, movementSpeed, 0);
            return true;
        }
        ChefScheduler.checkUnblock(level, maid);
        ChefScheduler.switchToNextContext(maid);
        return false;
    }

    private void onReached(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ExecutionNode readyNode = ChefScheduler.findReadyNode(maid);
        if (readyNode == null) return;

        assert readyNode.getRecipeNode().getCombineStep() != null;
        String capabilityUID = readyNode.getRecipeNode().getCombineStep().getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null || !capability.isValidWorkBlock(level, pos)) return;

        // 验证前置需求
        readyNode.verifyAndRollback(level, maid);
        MaidRestaurant.LOGGER.debug("ReadyNode State:" + readyNode.getState().name());
        if (readyNode.getState() != NodeState.READY) {
            return;
        }

        readyNode.setState(NodeState.EXECUTING);
        BlockUsageUtils.add(pos, maid.getUUID());

        BehaviorUtils.setTarget(maid, new BlockPosTracker(pos), TargetType.EXECUTE_COOK_STEP);
    }
}
