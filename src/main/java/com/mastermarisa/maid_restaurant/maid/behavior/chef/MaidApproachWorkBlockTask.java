package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.plan.PlanAction;
import com.mastermarisa.maid_restaurant.core.plan.Resolution;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.Progress;
import com.mastermarisa.maid_restaurant.data.task_data.WorkBlockCache;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class MaidApproachWorkBlockTask extends MaidCheckRateTask {
    public static final String UID = "ApproachWorkBlock";
    private static final double VERTICAL_TOLERANCE = 4.0;

    private final float movementSpeed;
    private final double closeEnoughDistSqr;
    private final Map<EntityMaid, ExecutionNode> pendingNode = new WeakHashMap<>();

    public MaidApproachWorkBlockTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) return false;

        PlanAction action = ChefScheduler.nextAction(level, maid);
        if (action == null || !isReadyToCook(action.resolution())) return false;

        pendingNode.put(maid, action.node());
        return true;
    }

    private static boolean isReadyToCook(Resolution resolution) {
        return resolution instanceof Resolution.Craft craft && craft.ready();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ExecutionNode node = pendingNode.get(maid);
        if (node == null) return;

        ICookCapability capability = node.getCapability();
        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (capability == null || zone == null) return;

        BlockPos workPos = getCachedPos(level, maid, node);
        if (workPos == null) workPos = capability.searchWorkBlock(level, zone, maid);

        if (workPos == null) {
            ChatBubbleUtil.setTextChatBubble(maid, Component.literal(
                    "主人,我找不到空闲的" + capability.getIcon().getDisplayName().getString() + "方块!"));
            return;
        }

        BlockPos target = workPos.immutable();
        if (isCloseEnough(maid, target)) {
            onReached(level, maid, target, node);
            return;
        }

        ChatBubbleUtil.removeChatBubble(maid);
        BlockPos walkPos = MergeUtil.mergeNullable(
                () -> PosUtil.findNearestSafePosHorizontal(level, maid, target),
                () -> PosUtil.findNearestSafePosHorizontal(level, maid, target.below()),
                () -> PosUtil.findNearestSafePosHorizontal(level, maid, target.above())
        );
        if (walkPos == null) walkPos = target.below();

        MemoryUtil.setTarget(maid, new BlockPosTracker(target), TargetType.APPROACH_WORK_BLOCK);
        MemoryUtil.setWalkAndLookTargetMemories(maid, walkPos, target, movementSpeed, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!MemoryUtil.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)) {
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
        ExecutionNode node = pendingNode.remove(maid);
        BlockPos target = getTargetPos(maid);
        if (node != null && target != null && isCloseEnough(maid, target)) {
            onReached(level, maid, target, node);
        }

        if (!MemoryUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP)) {
            MemoryUtil.removeTarget(maid);
        }
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void onReached(ServerLevel level, EntityMaid maid, BlockPos pos, ExecutionNode node) {
        if (BlockUsageUtil.isUsed(pos)) return;

        ICookCapability capability = node.getCapability();
        if (capability == null || !capability.isValidWorkBlock(level, pos)) {
            return;
        }

        node.setProgress(Progress.EXECUTING);
        BlockUsageUtil.add(pos, maid.getUUID());
        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.EXECUTE_COOK_STEP);
        maid.setData(ModTaskDataKeys.WORK_BLOCK_CACHE, new WorkBlockCache(pos, capability.getID()));
    }

    @Nullable
    private BlockPos getCachedPos(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        WorkBlockCache cache = maid.getData(ModTaskDataKeys.WORK_BLOCK_CACHE);
        ICookCapability capability = node.getCapability();
        if (cache == null || capability == null) return null;
        if (!capability.getID().equals(cache.getCapabilityID())) return null;

        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (zone == null) return null;

        BlockPos pos = cache.getPos();
        if (BlockUsageUtil.isUsed(pos) || !zone.contains(pos)) return null;
        if (!capability.isValidWorkBlock(level, pos)) return null;

        return pos;
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
