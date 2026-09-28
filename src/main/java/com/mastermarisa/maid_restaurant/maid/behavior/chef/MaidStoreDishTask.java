package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.request.CookingRequest;
import com.mastermarisa.maid_restaurant.core.request.ServingRequest;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.world.LevelRecipeLookup;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.ChefScheduler;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

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
        if (!super.checkExtraStartConditions(level, maid)) return false;

        CookingRequest request = ChefScheduler.getOrClaimRequest(level, maid);
        return request != null && holdsDish(maid, request.root);
    }

    private static boolean holdsDish(EntityMaid maid, ExecutionNode node) {
        return InvUtil.contains(maid.getAvailableInv(false), node.getIngredient(), node.getCount());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        CookingRequest request = ChefScheduler.getOrClaimRequest(level, maid);
        if (request == null || !holdsDish(maid, request.root)) return;

        ExecutionNode node = request.root;
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = InvUtil.extractFull(maidInv, node.getCount(),
                node.getIngredient(), true);
        if (results.isEmpty()) {
            return;
        }

        AbstractZone zone = ChefScheduler.getPrepZone(maid);
        if (zone == null) return;

        BlockPos target = findStorageTarget(level, zone, results);
        if (target == null) return;

        if (isCloseEnough(maid, target)) {
            storeDish(level, maid, target, request);
            return;
        }

        MemoryUtil.setTarget(maid, new BlockPosTracker(target), TargetType.STORE_DISH);
        MemoryUtil.setWalkAndLookTargetMemories(maid, target, target, movementSpeed, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!MemoryUtil.isTarget(maid, TargetType.STORE_DISH)) {
            return false;
        }
        BlockPos target = getTargetPos(maid);
        return target != null && !isCloseEnough(maid, target);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (gameTime % 10 != 0) return;
        BlockPos pos = getWalkTarget(maid);
        if (pos != null) {
            MemoryUtil.setIfAbsent(maid, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(pos, movementSpeed, 0));
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos target = getTargetPos(maid);
        if (target != null && isCloseEnough(maid, target)) {
            CookingRequest request = ChefScheduler.getOrClaimRequest(level, maid);
            if (request != null && holdsDish(maid, request.root)) {
                storeDish(level, maid, target, request);
            }
        }

        MemoryUtil.removeTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void storeDish(ServerLevel level, EntityMaid maid, BlockPos pos, CookingRequest request) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));

        ExecutionNode node = request.root;
        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> results = InvUtil.extractPartial(maidInv, node.getCount(),
                node.getIngredient(), true);
        if (results.isEmpty()) {
            return;
        }

        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) return;

        int inserted = 0;
        for (var stack : results) {
            inserted += stack.getCount() - storage.insert(level, pos, stack, false).getCount();
        }
        if (inserted == 0) return;

        InvUtil.extractFull(maidInv, inserted, node.getIngredient(), false);
        if (request.boundRequest != null) {
            request.boundRequest.sources.add(new ServingRequest.Source(pos, inserted));
        }

        if (node.getCount() - inserted <= 0) {
            ChefScheduler.submitRequest(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
        } else {
            request.root.applyCount(LevelRecipeLookup.of(level), node.getCount() - inserted);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
        }
    }

    @Nullable
    private BlockPos findStorageTarget(ServerLevel level, AbstractZone zone, List<ItemStack> results) {
        for (BlockPos pos : zone) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) continue;

            for (ItemStack stack : results) {
                int remain = storage.insert(level, pos, stack, true).getCount();
                if (remain < stack.getCount()) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos getTargetPos(EntityMaid maid) {
        return maid.getBrain().getMemory(ModEntities.TARGET_POS.get())
                .map(PositionTracker::currentBlockPosition)
                .orElse(null);
    }

    @Nullable
    private static BlockPos getWalkTarget(EntityMaid maid) {
        return maid.getBrain().getMemory(ModEntities.WALK_TARGET.get())
                .map(PositionTracker::currentBlockPosition)
                .orElse(null);
    }

    private boolean isCloseEnough(EntityMaid maid, BlockPos pos) {
        double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
        double distVertical = Math.abs(maid.getY() - pos.getY());
        return distHorizontal <= closeEnoughDistSqr && distVertical <= 4;
    }
}