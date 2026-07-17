package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidExecuteCookStepTask extends MaidTickRateTask {
    public MaidExecuteCookStepTask() {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT), 10);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) {
            return false;
        }

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        if (BlockUsageUtils.isUsed(pos) && !BlockUsageUtils.isUsing(pos, maid.getUUID())) {
            return false;
        }

        RecipeStep step = node.getRecipeNode().getCombineStep();
        if (step == null) {
            return false;
        }

        String capabilityUID = step.getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        return capability != null && capability.isValidWorkBlock(level, pos);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidExecuteCookStepTask - START");
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
        if (!shouldTick(level, maid, gameTime)) {
            return;
        }

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) {
            return;
        }

        RecipeStep step = node.getRecipeNode().getCombineStep();
        if (step == null) {
            return;
        }

        String capabilityUID = step.getCapabilityUID();
        ICookCapability capability = CapabilityRegistry.get(capabilityUID);
        if (capability == null) {
            return;
        }

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).orElseThrow().currentBlockPosition();
        CookResult result = capability.cookTick(level, maid, pos, step);
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos.above()));

        if (result == CookResult.DONE) {
            if (node.getParent() == null) {
                ChefScheduler.trySubmitRequest(level, maid);
                node.verifyAndUpdateState(level, maid);
            } else {
                node.verifyAndUpdateState(level, maid);
                node.getParent().computeState();
                MaidRestaurant.LOGGER.debug("Node State: " + node.getState());
//                if (node.getState() == NodeState.EXECUTING) {
//                    node.setState(NodeState.DONE);
//                    node.getParent().computeState();
//                }
            }
        } else if (result == CookResult.INTERRUPTED) {
            node.verifyAndUpdateState(level, maid);
        }
        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);
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
