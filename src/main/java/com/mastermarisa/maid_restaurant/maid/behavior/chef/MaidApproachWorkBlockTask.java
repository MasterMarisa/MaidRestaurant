package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.data.task_data.WorkBlockCache;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.uitls.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Nullable;

public class MaidApproachWorkBlockTask extends MaidCheckRateTask {
    public static final String UID = "ApproachWorkBlock";

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidApproachWorkBlockTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
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

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.READY);
        if (node == null) {
            return false;
        }

        node.verifyAndUpdateState(level, maid);
        if (node.getState() != NodeState.READY) {
            node.computeParentState();
            if (node.getParent() != null && node.getParent().getState() == NodeState.READY) {
                CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
            }
            return false;
        }

        return searchWorkBlock(level, maid, node);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidApproachWorkBlockTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return MemoryUtil.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)
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
                        onReached(level, maid, pos);
                    }
                });
        MemoryUtil.removeTargetIfMatch(maid, TargetType.APPROACH_WORK_BLOCK);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private boolean searchWorkBlock(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        ICookCapability capability = node.getCapability();
        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (capability == null || zone == null) {
            return false;
        }

        BlockPos workPos = getCachedPos(level, maid, node);
        if (workPos == null) {
            workPos = capability.searchWorkBlock(level, zone, maid);
        }

        if (workPos != null) {
            BlockPos finalWorkPos = workPos;
            BlockPos walkPos = MergeUtil.mergeNullable(
                    () -> PosUtil.findNearestSafePosHorizontal(level, maid, finalWorkPos),
                    () -> PosUtil.findNearestSafePosHorizontal(level, maid, finalWorkPos.below()),
                    () -> PosUtil.findNearestSafePosHorizontal(level, maid, finalWorkPos.above())
            );
            if (walkPos == null) {
                walkPos = workPos.below();
            }
            ChatBubbleUtil.removeChatBubble(maid);
            MemoryUtil.setTarget(maid, new BlockPosTracker(workPos), TargetType.APPROACH_WORK_BLOCK);
            MemoryUtil.setWalkAndLookTargetMemories(maid, walkPos, workPos, movementSpeed, 0);
            return true;
        }

        ChatBubbleUtil.setTextChatBubble(maid, Component.literal("主人,我找不到空闲的" + capability.getIcon().getDisplayName().getString() + "方块!"));
        return false;
    }

    private void onReached(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (BlockUsageUtil.isUsed(pos)) {
            return;
        }

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.READY);
        if (node == null) {
            return;
        }

        node.verifyAndUpdateState(level, maid);
        if (node.getState() != NodeState.READY) {
            node.computeParentState();
            if (node.getParent() != null && node.getParent().getState() == NodeState.READY) {
                CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
            }
            return;
        }

        ICookCapability capability = node.getCapability();
        if (capability == null || !capability.isValidWorkBlock(level, pos)) {
            return;
        }

        node.setState(NodeState.EXECUTING);
        BlockUsageUtil.add(pos, maid.getUUID());
        MemoryUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.EXECUTE_COOK_STEP);
        maid.setData(ModTaskDataKeys.WORK_BLOCK_CACHE, new WorkBlockCache(pos, capability.getID()));
    }

    @Nullable
    private BlockPos getCachedPos(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        WorkBlockCache cache = maid.getData(ModTaskDataKeys.WORK_BLOCK_CACHE);
        ICookCapability capability = node.getCapability();
        if (cache == null || capability == null) {
            return null;
        }

        if (!capability.getID().equals(cache.getCapabilityID())) {
            return null;
        }

        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        BlockPos pos = cache.getPos();
        if (BlockUsageUtil.isUsed(pos) || zone == null || !zone.contains(pos)) {
            return null;
        }

        if (!capability.isValidWorkBlock(level, pos)) {
            return null;
        }
        return pos;
    }
}
