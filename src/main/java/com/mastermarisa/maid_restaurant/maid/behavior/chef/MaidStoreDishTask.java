package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class MaidStoreDishTask extends MaidCheckRateTask {
    public static final String UID = "StoreDish";

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidStoreDishTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
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
        return searchTarget(level, maid, request.root);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidStoreDishTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return MemoryUtil.isTarget(maid, TargetType.STORE_DISH)
                && maid.getBrain()
                .getMemory(ModEntities.TARGET_POS.get())
                .map(tracker -> {
                    BlockPos pos = tracker.currentBlockPosition();
                    double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
                    double distVertical = Math.abs(maid.getY() - pos.getY());
                    return distHorizontal > closeEnoughDistSqr || distVertical > 4;
                }).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (gameTime % 10 != 0) {
            return;
        }
        maid.getBrain()
                .getMemory(ModEntities.STAND_POS.get())
                .ifPresent(tracker -> {
                    BlockPos pos = tracker.currentBlockPosition();
                    WalkTarget target = new WalkTarget(pos, movementSpeed, 0);
                    MemoryUtil.setIfAbsent(maid, MemoryModuleType.WALK_TARGET, target);
                });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain()
                .getMemory(ModEntities.TARGET_POS.get())
                .ifPresent(tracker -> {
                    BlockPos pos = tracker.currentBlockPosition();
                    double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
                    double distVertical = Math.abs(maid.getY() - pos.getY());
                    if (distHorizontal <= closeEnoughDistSqr && distVertical <= 4) {
                        storeDish(level, maid, pos);
                    }
                });
        MemoryUtil.removeTargetIfMatch(maid, TargetType.STORE_DISH);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchTarget(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = InvUtil.tryExtract(maidInv, node.getCount(), node.getIngredient(), true, true);
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
            MemoryUtil.setTarget(maid, new BlockPosTracker(best), TargetType.STORE_DISH);
            MemoryUtil.setWalkAndLookTargetMemories(maid, best, best, movementSpeed, 0);
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
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = InvUtil.tryExtract(maidInv, node.getCount(), node.getIngredient(), true, true);
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
        InvUtil.tryExtract(maidInv, inserted, node.getIngredient(), true, false);

        if (node.getCount() - inserted <= 0) {
            ChefScheduler.submitRequest(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        } else {
            request.root.applyCount(level, node.getCount() - inserted);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
        }
    }
}
