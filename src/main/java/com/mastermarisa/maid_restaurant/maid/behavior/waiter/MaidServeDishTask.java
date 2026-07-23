package com.mastermarisa.maid_restaurant.maid.behavior.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.ServeRequestBus;
import com.mastermarisa.maid_restaurant.schedule.WaiterScheduler;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class MaidServeDishTask extends MaidCheckRateTask {
    public static final String UID = "ServeDish";

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidServeDishTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
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

        return searchTarget(level, maid, request);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return MemoryUtil.isTarget(maid, TargetType.SERVE_DISH)
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
                        acceptTarget(level, maid, pos);
                    }
                });
        MemoryUtil.removeTargetIfMatch(maid, TargetType.SERVE_DISH);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchTarget(ServerLevel level, EntityMaid maid, ServeRequest request) {
        if (!request.pickupPoints.isEmpty()) {
            return false;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        if (request.targets.isEmpty() || InvUtil.count(maidInv, request.dish) <= 0) {
            ServeRequestBus.getInstance(level).submit("chef", maid);
            return false;
        }

        ServeRequest.Target target = request.targets.get(0);
        BlockPos pos = target.pos();

        if (target.type() == 0) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) {
                request.targets.remove(0);
                return false;
            }
        } else if (target.type() == 1) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                request.targets.remove(0);
                return false;
            }
        }

        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.SERVE_DISH);
        MemoryUtil.setWalkAndLookTargetMemories(maid, pos, pos, movementSpeed, 0);
        return true;
    }

    private void acceptTarget(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ServeRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null) {
            return;
        }

        if (request.targets.isEmpty()) {
            ServeRequestBus.getInstance(level).submit("chef", maid);
            return;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        ServeRequest.Target target = request.targets.remove(0);
        List<ItemStack> toInsert = InvUtil.tryExtract(maidInv, request.count, request.dish, false);
        if (toInsert.isEmpty()) {
            ServeRequestBus.getInstance(level).submit("chef", maid);
            return;
        }

        if (target.type() == 0) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage != null) {
                for (var stack : toInsert) {
                    ItemStack restItem = storage.insert(level, pos, stack, false);
                    int inserted = stack.getCount() - restItem.getCount();
                    if (inserted == 0) {
                        break;
                    }
                    stack.shrink(inserted);
                }
            }
        } else if (target.type() == 1) {
            if(level.getBlockState(pos).canBeReplaced()) {
                toInsert.stream()
                        .filter(s -> s.getItem() instanceof BlockItem)
                        .findAny()
                        .ifPresent(stack -> {
                            Direction dir = PosUtil.getHorizontalDirection(pos.getX() - maid.getX(), pos.getZ() - maid.getZ());
                            maid.placeItemBlock(InteractionHand.MAIN_HAND, pos, dir, stack.split(1));
                            maid.swing(InteractionHand.MAIN_HAND);
                        });
            }
        }

        for (var stack : toInsert) {
            if (!stack.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(maidInv, stack, false);
            }
        }

        if (request.targets.isEmpty()) {
            ServeRequestBus.getInstance(level).submit("chef", maid);
        }
        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
    }
}
