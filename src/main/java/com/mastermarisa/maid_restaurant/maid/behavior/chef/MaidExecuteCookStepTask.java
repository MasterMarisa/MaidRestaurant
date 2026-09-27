package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import com.mastermarisa.maid_restaurant.uitls.ChefScheduler;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Nullable;

public class MaidExecuteCookStepTask extends MaidTickRateTask {
    private static final double VERTICAL_TOLERANCE = 4.0;
    private static final int DEFAULT_INTERVAL = 20;

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidExecuteCookStepTask(float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.movementSpeed = movementSpeed;
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) return false;

        BlockPos pos = getTargetPos(maid);
        if (pos == null) return false;

        if (!BlockUsageUtil.isUsing(pos, maid.getUUID())) return false;

        if (Math.abs(maid.getY() - pos.getY()) > VERTICAL_TOLERANCE) return false;

        ICookCapability capability = node.getCapability();
        return capability != null && capability.isValidWorkBlock(level, pos);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = getTargetPos(maid);
        if (pos == null) return;

        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        this.ticksRemain = 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        if (ticksRemain > 0) return true;
        return MemoryUtil.isTarget(maid, TargetType.EXECUTE_COOK_STEP)
                && checkExtraStartConditions(level, maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = getTargetPos(maid);
        if (pos == null) return;

        if (gameTime % 10 == 0) {
            maintainMovement(maid, pos);
        }

        if (!shouldTick(level, maid, gameTime)) return;

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) return;

        ICookCapability capability = node.getCapability();
        if (capability == null) return;

        CookResult result = capability.cookTick(level, maid, pos, node.getRecipeNode());
        CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidApproachWorkBlockTask.UID, 5);

        switch (result) {
            case DONE -> {
                CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidStoreDishTask.UID, 5);
                doStop(level, maid, gameTime);
            }
            case INTERRUPTED -> {
                CheckRateHelper.setRemainingTicks(maid.getUUID(), MaidGatherMaterialTask.UID, 5);
                doStop(level, maid, gameTime);
            }
            default -> { }
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = getTargetPos(maid);
        if (pos != null) {
            BlockUsageUtil.remove(pos, maid.getUUID());
        }
        MemoryUtil.removeTarget(maid);

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node != null) {
            node.verifyAndUpdateState(level, maid);
            node.computeParentState();
        }
    }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    @Override
    protected int getInterval(ServerLevel level, EntityMaid maid) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.EXECUTING);
        if (node == null) return DEFAULT_INTERVAL;
        ICookCapability capability = node.getCapability();
        return capability != null ? capability.getTickInterval() : DEFAULT_INTERVAL;
    }

    private void maintainMovement(EntityMaid maid, BlockPos pos) {
        double distHorizontal = PosUtil.distSqrHorizontal(maid, pos);
        if (distHorizontal <= closeEnoughDistSqr) return;

        BlockPos walkPos = getWalkTarget(maid);
        if (walkPos == null) return;

        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(walkPos, movementSpeed, 0));
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(pos));
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
}