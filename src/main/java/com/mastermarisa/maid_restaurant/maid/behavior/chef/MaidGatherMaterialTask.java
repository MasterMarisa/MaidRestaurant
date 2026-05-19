package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.recipe.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.recipe.NodeState;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.crafting.Ingredient;

public class MaidGatherMaterialTask extends MaidCheckRateTask {
    public static final String UID = "GatherMaterial";

    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidGatherMaterialTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) return false;
        MaidRestaurant.LOGGER.debug("checked");
        ExecutionNode node = ChefScheduler.findNeedMaterialNode(maid);
        if (node == null) return false;
        MaidRestaurant.LOGGER.debug("node found");
        int count = ItemUtils.count(maid.getAvailableInv(true), node.getRecipeNode().getOutput());
        if (count >= node.getRecipeNode().getOutputCount()) {
            node.setState(NodeState.DONE);
            if (node.getParent() != null) {
                node.getParent().computeState();
            }
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 1);
            return false;
        }
        return searchStorage(level, maid, node);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.isTarget(maid, TargetType.GATHER_MATERIAL)
                && maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
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
                acceptStorage(level, maid, pos);
            }
        });
        if (BehaviorUtils.isTarget(maid, TargetType.GATHER_MATERIAL)) BehaviorUtils.eraseTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private boolean searchStorage(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        Ingredient ingredient = node.getRecipeNode().getOutput();
        int required = node.getRecipeNode().getOutputCount() - ItemUtils.count(maid.getAvailableInv(true), ingredient);
        RestaurantZone zone = RestaurantZone.getZone(maid);

        if (zone == null || !zone.isValid()) return false;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos center = maid.blockPosition();

        for (int x = zone.getMin().getX(); x <= zone.getMax().getX(); x++) {
            for (int y = zone.getMin().getY(); y <= zone.getMax().getY(); y++) {
                for (int z = zone.getMin().getZ(); z <= zone.getMax().getZ(); z++) {
                    mutable.set(x, y, z);
                    IMaidStorage storage = StorageRegistry.tryGetAt(level, mutable);
                    if (storage != null && storage.count(level, mutable, ingredient) >= required) {
                        double dist = mutable.distSqr(center);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = mutable.immutable();
                        }
                    }
                }
            }
        }

        if (best != null) {
            BehaviorUtils.setTarget(maid, new BlockPosTracker(best), TargetType.GATHER_MATERIAL);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, best, best, movementSpeed, 0);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);
            return true;
        }
        ChefScheduler.checkUnblock(level, maid);
        ChefScheduler.switchToNextContext(maid);
        return false;
    }

    private void acceptStorage(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ExecutionNode node = ChefScheduler.findNeedMaterialNode(maid);
        if (node == null || !node.isLeaf()) return;

        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) return;

        Ingredient ingredient = node.getRecipeNode().getOutput();
        int required = node.getRecipeNode().getOutputCount() - ItemUtils.count(maid.getAvailableInv(true), ingredient);

        maid.swing(InteractionHand.OFF_HAND);
        if (ItemUtils.tryTake(level, pos, storage, maid.getAvailableInv(false), ingredient, required)) {
            node.setState(NodeState.DONE);
        }

        ExecutionNode parent = node.getParent();
        if (parent != null) {
            parent.computeState();
        }

        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
    }
}
