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
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtil;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import com.mastermarisa.maid_restaurant.uitls.ChatBubbleUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

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
        return searchWorkBlock(level, maid, node);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidApproachWorkBlockTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtil.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)
                && maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker -> {
                    BlockPos pos = tracker.currentBlockPosition();
                    double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
                    double distVertical = Math.abs(maid.getY() - pos.getY());
                    return distHorizontal > closeEnoughDistSqr || distVertical > 4;
                }).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (gameTime % 10 != 0) return;
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BlockPos pos = tracker.currentBlockPosition();
            BehaviorUtil.setWalkAndLookTargetMemories(maid, pos.below(), pos, movementSpeed, 0);
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BlockPos pos = tracker.currentBlockPosition();
            double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
            double distVertical = Math.abs(maid.getY() - pos.getY());
            if (distHorizontal <= closeEnoughDistSqr && distVertical <= 4) {
                onReached(level, maid, pos);
            }
        });
        if (BehaviorUtil.isTarget(maid, TargetType.APPROACH_WORK_BLOCK)) {
            BehaviorUtil.eraseTarget(maid);
        }
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchWorkBlock(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        ICookCapability capability = node.getCapability();
        if (capability == null) {
            return false;
        }

        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (zone == null) {
            return false;
        }

        WorkBlockCache cache = maid.getData(ModTaskDataKeys.WORK_BLOCK_CACHE);
        if (cache != null && capability.getUID().equals(cache.getCapabilityUID())) {
            BlockPos cachedPos = cache.getPos();
            if (capability.isValidWorkBlock(level, cachedPos) && !BlockUsageUtil.isUsed(cachedPos) && zone.contains(cachedPos)) {
                ChatBubbleUtil.removeChatBubble(maid);
                BehaviorUtil.setTarget(maid, new BlockPosTracker(cachedPos), TargetType.APPROACH_WORK_BLOCK);
                BehaviorUtil.setWalkAndLookTargetMemories(maid, cachedPos.below(), cachedPos, movementSpeed, 1);
                return true;
            }
        }

        BlockPos workPos = capability.searchWorkBlock(level, zone, maid);
        if (workPos != null) {
            ChatBubbleUtil.removeChatBubble(maid);
            BehaviorUtil.setTarget(maid, new BlockPosTracker(workPos), TargetType.APPROACH_WORK_BLOCK);
            BehaviorUtil.setWalkAndLookTargetMemories(maid, workPos.below(), workPos, movementSpeed, 1);
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

        ICookCapability capability = node.getCapability();
        if (capability == null || !capability.isValidWorkBlock(level, pos)) {
            return;
        }

        node.verifyAndUpdateState(level, maid);
        if (node.getState() != NodeState.READY) {
            return;
        }

        node.setState(NodeState.EXECUTING);
        BehaviorUtil.setTarget(maid, new BlockPosTracker(pos), TargetType.EXECUTE_COOK_STEP);
        BlockUsageUtil.add(pos, maid.getUUID());
        maid.setData(ModTaskDataKeys.WORK_BLOCK_CACHE, new WorkBlockCache(pos, capability.getUID()));
    }
}
