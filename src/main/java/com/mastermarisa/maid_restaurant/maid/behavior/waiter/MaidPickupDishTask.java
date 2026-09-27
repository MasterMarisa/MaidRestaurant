package com.mastermarisa.maid_restaurant.maid.behavior.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.request.ServingRequest;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import com.mastermarisa.maid_restaurant.uitls.WaiterScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class MaidPickupDishTask extends MaidCheckRateTask {
    public static final String UID = "PickupDish";
    private static final double VERTICAL_TOLERANCE = 4.0;

    private final float movementSpeed;
    private final double closeEnoughDistSqr;
    private final Map<EntityMaid, BlockPos> pendingSource = new WeakHashMap<>();

    public MaidPickupDishTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) return false;
        ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null) return false;

        BlockPos pos = findFirstValid(level, request);
        if (pos == null) return false;
        pendingSource.put(maid, pos);
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = pendingSource.remove(maid);
        if (pos == null) return;

        ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null) return;

        pruneBefore(request, pos);

        if (isCloseEnough(maid, pos)) {
            take(level, maid, request, pos);
            return;
        }

        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.PICKUP_DISH);
        MemoryUtil.setWalkAndLookTargetMemories(maid, pos, pos, movementSpeed, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!MemoryUtil.isTarget(maid, TargetType.PICKUP_DISH)) {
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
        pendingSource.remove(maid);

        BlockPos target = getTargetPos(maid);
        if (target != null && isCloseEnough(maid, target)) {
            ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
            if (request != null) {
                take(level, maid, request, target);
            }
        }

        MemoryUtil.removeTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void take(ServerLevel level, EntityMaid maid, ServingRequest request, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        if (request.sources.isEmpty()) return;

        ServingRequest.Source source = request.getFirstSource();
        if (!source.pos().equals(pos)) return;

        request.removeFirstSource();
        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) return;

        maid.swing(InteractionHand.OFF_HAND);
        IItemHandler maidInv = maid.getAvailableInv(false);
        InvUtil.take(level, pos, storage, maidInv, request.dish, source.count());
        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 1);
    }

    @Nullable
    private BlockPos findFirstValid(ServerLevel level, ServingRequest request) {
        for (ServingRequest.Source source : request.sources) {
            if (isValidSource(level, request, source)) {
                return source.pos();
            }
        }
        return null;
    }

    private void pruneBefore(ServingRequest request, BlockPos pos) {
        Iterator<ServingRequest.Source> it = request.sources.iterator();
        while (it.hasNext()) {
            ServingRequest.Source source = it.next();
            if (source.pos().equals(pos)) {
                break;
            }
            it.remove();
        }
    }

    private boolean isValidSource(ServerLevel level, ServingRequest request, ServingRequest.Source source) {
        IMaidStorage storage = StorageRegistry.tryGetAt(level, source.pos());
        return storage != null && storage.count(level, source.pos(), request.dish) > 0;
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
        return distHorizontal <= closeEnoughDistSqr && distVertical <= VERTICAL_TOLERANCE;
    }
}
