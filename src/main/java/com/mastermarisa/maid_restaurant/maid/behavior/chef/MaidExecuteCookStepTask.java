package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.CookStep;
import com.mastermarisa.maid_restaurant.core.recipe.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.recipe.NodeState;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidExecuteCookStepTask extends MaidTickRateTask {
    public MaidExecuteCookStepTask() {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT), 5);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findExecutingNode(maid);
        if (node == null) return false;
        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        if (BlockUsageUtils.isUsed(pos) && !BlockUsageUtils.isUsing(pos, maid.getUUID())) return false;

        assert node.getRecipeNode().getCombineStep() != null;
        String capabilityUID = node.getRecipeNode().getCombineStep().getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        return capability != null && capability.isValidWorkBlock(level, pos);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        if (ticksRemain > 0){
            return true;
        } else {
            return BehaviorUtils.isTarget(maid, TargetType.EXECUTE_COOK_STEP) && checkExtraStartConditions(level, maid);
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level, maid, gameTime)) return;

        ExecutionNode node = ChefScheduler.findExecutingNode(maid);
        if (node == null) return;

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        assert node.getRecipeNode().getCombineStep() != null;
        String capabilityUID = node.getRecipeNode().getCombineStep().getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null) return;

        CookStep step = node.getRecipeNode().getCombineStep();
        CookResult result = capability.cookTick(level, maid, pos, step);

        if (result == CookResult.DONE) {
            if (node.getParent() == null) {
                ChefScheduler.checkAndSubmit(level, maid);
            } else {
                node.setState(NodeState.DONE);
                node.getParent().computeState();
            }
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);
        } else if (result == CookResult.INTERRUPTED) {
            node.verifyAndRollback(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BlockUsageUtils.remove(tracker.currentBlockPosition(), maid.getUUID());
        });
        if (BehaviorUtils.isTarget(maid, TargetType.EXECUTE_COOK_STEP)) {
            BehaviorUtils.eraseTarget(maid);
        }
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }
}
