package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MaidGatherMaterialTask extends MaidCheckRateTask {
    public static final String UID = "GatherMaterial";
    private static final double VERTICAL_TOLERANCE = 4.0;

    private final float movementSpeed;
    private final double closeEnoughDistSqr;
    private final Map<EntityMaid, ExecutionNode> pendingNode = new WeakHashMap<>();

    public MaidGatherMaterialTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maxInterval, 60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDistSqr = closeEnoughDist * closeEnoughDist;
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) return false;

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.NEED_MATERIALS);
        if (node == null) return false;

        pendingNode.put(maid, node);
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ExecutionNode node = pendingNode.remove(maid);
        if (node == null) return;

        node.verifyAndUpdateState(level, maid);
        if (node.getState() != NodeState.NEED_MATERIALS) {
            node.computeParentState();
            if (node.getParent() != null && node.getParent().getState() == NodeState.WAITING) {
                CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
            }
            return;
        }

        Ingredient ingredient = node.getIngredient();
        if (ingredient.isEmpty()) return;

        AbstractZone zone = ChefScheduler.getStorageZone(maid);
        if (zone == null) return;

        BlockPos best = findNearestStorage(level, maid, zone, ingredient);
        if (best == null) {
            ChatBubbleUtil.setTextChatBubble(maid, Component.literal(
                    "主人,我缺少" + ingredient.getItems()[0].getDisplayName().getString() + "!"));
            return;
        }

        if (isCloseEnough(maid, best)) {
            take(level, maid, best, node);
            return;
        }

        ChatBubbleUtil.removeChatBubble(maid);
        MemoryUtil.setTarget(maid, new BlockPosTracker(best), TargetType.GATHER_MATERIAL);
        MemoryUtil.setWalkAndLookTargetMemories(maid, best, best, movementSpeed, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!MemoryUtil.isTarget(maid, TargetType.GATHER_MATERIAL)) {
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
        pendingNode.remove(maid);

        BlockPos target = getTargetPos(maid);
        if (target != null && isCloseEnough(maid, target)) {
            ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.NEED_MATERIALS);
            if (node != null) {
                take(level, maid, target, node);
            }
        }

        MemoryUtil.removeTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void take(ServerLevel level, EntityMaid maid, BlockPos pos, ExecutionNode node) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        try {
            IItemHandler maidInv = maid.getAvailableInv(false);
            int count = node.calculateRequiredCount(level, maid);
            if (count > 0) {
                List<ItemStack> existed = ChefScheduler.getExistedInputs(level, maid, node.getParent());
                count -= InvUtil.count(existed, node.getIngredient());
            }

            if (count <= 0) {
                node.setState(NodeState.DONE);
                node.computeParentState();
                return;
            }

            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) return;

            maid.swing(InteractionHand.OFF_HAND);
            if (InvUtil.take(level, pos, storage, maidInv, node.getIngredient(), count)) {
                node.setState(NodeState.DONE);
                node.computeParentState();
            }
        } finally {
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
        }
    }

    @Nullable
    private BlockPos findNearestStorage(ServerLevel level, EntityMaid maid,
                                        AbstractZone zone, Ingredient ingredient) {
        BlockPos center = maid.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : zone) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) continue;
            if (storage.count(level, pos, ingredient) <= 0) continue;

            double dist = pos.distSqr(center);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
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