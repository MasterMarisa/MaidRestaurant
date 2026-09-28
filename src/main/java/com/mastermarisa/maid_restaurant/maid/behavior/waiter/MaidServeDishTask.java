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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
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
        if (!super.checkExtraStartConditions(level, maid)) return false;

        ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null) return false;

        return request.sources.isEmpty();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
        if (request == null || !request.sources.isEmpty()) return;

        IItemHandler maidInv = maid.getAvailableInv(false);
        if (request.count == 0 || request.targets.isEmpty() || InvUtil.count(maidInv, request.dish) <= 0) {
            WaiterScheduler.submitRequest(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
            return;
        }

        ServingRequest.Target target = findValidTarget(level, request);
        if (target == null) {
            WaiterScheduler.submitRequest(level, maid);
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
            return;
        }

        BlockPos pos = target.pos();
        if (isCloseEnough(maid, pos)) {
            acceptTarget(level, maid, pos, request);
            return;
        }

        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.SERVE_DISH);
        MemoryUtil.setWalkAndLookTargetMemories(maid, pos, pos, movementSpeed, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!MemoryUtil.isTarget(maid, TargetType.SERVE_DISH)) {
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
            ServingRequest request = WaiterScheduler.getOrClaimRequest(level, maid);
            if (request != null && request.sources.isEmpty()) {
                acceptTarget(level, maid, target, request);
            }
        }

        MemoryUtil.removeTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void acceptTarget(ServerLevel level, EntityMaid maid, BlockPos pos, ServingRequest request) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        if (request.targets.isEmpty()) {
            WaiterScheduler.submitRequest(level, maid);
            return;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        ServingRequest.Target target = request.targets.remove(0);
        List<ItemStack> toInsert = InvUtil.extractPartial(maidInv, request.count, request.dish, false);
        if (toInsert.isEmpty()) {
            WaiterScheduler.submitRequest(level, maid);
            return;
        }

        if (target.type() == 0) {
            insertIntoStorage(level, pos, request, toInsert);
        } else if (target.type() == 1) {
            placeBlockAt(level, maid, pos, request, toInsert);
        }

        for (ItemStack stack : toInsert) {
            if (!stack.isEmpty()) {
                InvUtil.getItemToMaid(maid, stack);
            }
        }

        if (request.targets.isEmpty()) {
            WaiterScheduler.submitRequest(level, maid);
        }
        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
    }

    private void insertIntoStorage(ServerLevel level, BlockPos pos,
                                   ServingRequest request, List<ItemStack> toInsert) {
        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) return;

        for (ItemStack stack : toInsert) {
            ItemStack rest = storage.insert(level, pos, stack.copy(), false);
            int inserted = stack.getCount() - rest.getCount();
            if (inserted == 0) break;
            stack.shrink(inserted);
            request.count -= inserted;
        }
    }

    private void placeBlockAt(ServerLevel level, EntityMaid maid, BlockPos pos,
                              ServingRequest request, List<ItemStack> toInsert) {
        if (!level.getBlockState(pos).canBeReplaced()) return;
        if (!level.getEntities(null, new AABB(pos)).isEmpty()) return;

        toInsert.stream()
                .filter(s -> s.getItem() instanceof BlockItem)
                .findAny()
                .ifPresent(stack -> {
                    Direction dir = PosUtil.getHorizontalDirection(
                            pos.getX() - maid.getX(), pos.getZ() - maid.getZ());
                    ItemStack toPlace = stack.copyWithCount(1);
                    if (maid.placeItemBlock(InteractionHand.MAIN_HAND, pos, dir, toPlace)) {
                        stack.shrink(1);
                        request.count--;
                    }
                    maid.swing(InteractionHand.MAIN_HAND);
                });
    }

    private boolean isValidTarget(ServerLevel level, BlockPos pos, int type, ItemStack dish) {
        return switch (type) {
            case 0 -> StorageRegistry.tryGetAt(level, pos) != null;
            case 1 -> level.getBlockState(pos).canBeReplaced() && dish.getItem() instanceof BlockItem;
            default -> true;
        };
    }

    @Nullable
    private ServingRequest.Target findValidTarget(ServerLevel level, ServingRequest request) {
        ItemStack itemStack = request.dish.getItems()[0];
        Iterator<ServingRequest.Target> it = request.targets.iterator();
        while (it.hasNext()) {
            ServingRequest.Target target = it.next();
            if (!isValidTarget(level, target.pos(), target.type(), itemStack)) {
                it.remove();
                continue;
            }
            return target;
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
