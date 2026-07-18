package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequest;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class MaidStoreDishTask extends MaidCheckRateTask {
    public static final String UID = "StoreDish";

    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidStoreDishTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
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
        CookingRequest request = ChefScheduler.getOrClaimRequest(level, maid);
        if (request == null || request.root.getState() != NodeState.DONE) {
            return false;
        }
        return searchTarget(level, maid, request);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.isTarget(maid, TargetType.STORE_DISH)
                && maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
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
                storeDish(level, maid, pos);
            }
        });
        if (BehaviorUtils.isTarget(maid, TargetType.STORE_DISH)) BehaviorUtils.eraseTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private boolean searchTarget(ServerLevel level, EntityMaid maid, CookingRequest request) {
        ExecutionNode node = request.root;
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = ItemUtils.tryExtract(maidInv, request.count, node.getRecipeNode().getOutput(), true, true);
        if (results.isEmpty()) {
            node.verifyAndUpdateState(level, maid);
            return false;
        }

        AbstractZone zone = ChefScheduler.getPrepZone(maid);
        if (zone == null) {
            return false;
        }

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos center = maid.blockPosition();

        for (BlockPos pos : zone) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) {
                continue;
            }
            if (results.stream().anyMatch(s ->
                    s.getCount() > storage.insert(level, pos, s, true).getCount())) {
                double dist = pos.distSqr(center);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos;
                }
            }
        }

        if (best != null) {
            BehaviorUtils.setTarget(maid, new BlockPosTracker(best), TargetType.STORE_DISH);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, best, best, movementSpeed, 0);
            return true;
        }

        return false;
    }

    private void storeDish(ServerLevel level, EntityMaid maid, BlockPos pos) {
        CookingRequest request = ChefScheduler.getOrClaimRequest(level, maid);
        if (request == null || request.root.getState() != NodeState.DONE) {
            return;
        }

        ExecutionNode node = request.root;
        Ingredient ingredient = node.getRecipeNode().getOutput();
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = ItemUtils.tryExtract(maidInv, request.count, ingredient, true, true);
        if (results.isEmpty()) {
            node.verifyAndUpdateState(level, maid);
            return;
        }

        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) {
            return;
        }

        int inserted = 0;
        for (var stack : results) {
            inserted += stack.getCount() - storage.insert(level, pos, stack, false).getCount();
        }
        if (inserted == 0) {
            return;
        }
        ItemUtils.tryExtract(maidInv, inserted, ingredient, true, false);

        request.count -= inserted;
        if (request.count <= 0) {
            ChefScheduler.submitRequest(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        }
    }
}
