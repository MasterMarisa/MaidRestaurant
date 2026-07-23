package com.mastermarisa.maid_restaurant.maid.behavior.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.WaiterScheduler;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

public class MaidPickupDishTask extends MaidCheckRateTask {
    public static final String UID = "PickupDish";

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidPickupDishTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
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

        ServeRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null) {
            return false;
        }

        return searchStorage(level, maid, request);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return MemoryUtil.isTarget(maid, TargetType.PICKUP_DISH)
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
                        acceptStorage(level, maid, pos);
                    }
                });
        MemoryUtil.removeTargetIfMatch(maid, TargetType.PICKUP_DISH);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchStorage(ServerLevel level, EntityMaid maid, ServeRequest request) {
        if (request.pickupPoints.isEmpty()) {
            return false;
        }

        ServeRequest.Source source = request.pickupPoints.get(0);
        BlockPos pos = source.pos();
        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null || storage.count(level, pos, request.dish) <= 0) {
            request.pickupPoints.remove(0);
            return searchStorage(level, maid, request);
        }

        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.PICKUP_DISH);
        MemoryUtil.setWalkAndLookTargetMemories(maid, pos, pos, movementSpeed, 0);
        return true;
    }

    private void acceptStorage(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ServeRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null || request.pickupPoints.isEmpty()) {
            return;
        }

        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) {
            return;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        ServeRequest.Source source = request.pickupPoints.get(0);
        InvUtil.tryTake(level, pos, storage, maidInv, request.dish, source.count());
        request.pickupPoints.remove(0);
        maid.swing(InteractionHand.OFF_HAND);

        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
    }
}
