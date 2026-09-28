package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.plan.PlanAction;
import com.mastermarisa.maid_restaurant.core.plan.Resolution;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.Progress;
import com.mastermarisa.maid_restaurant.core.world.LevelRecipeLookup;
import com.mastermarisa.maid_restaurant.core.world.MaidWorldView;
import com.mastermarisa.maid_restaurant.core.world.WorldContext;
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

        PlanAction action = ChefScheduler.nextAction(level, maid);
        if (action == null) return false;

        if (!(action.resolution() instanceof Resolution.Fetch)
                && !(action.resolution() instanceof Resolution.Impossible)) {
            return false;
        }

        pendingNode.put(maid, action.node());
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ExecutionNode node = pendingNode.get(maid);
        if (node == null) return;

        Ingredient ingredient = node.getIngredient();
        if (ingredient.isEmpty()) {
            pendingNode.remove(maid);
            return;
        }

        if (ChefScheduler.getStorageZone(maid) == null) return;

        Resolution resolution = node.getResolution();
        if (!(resolution instanceof Resolution.Fetch fetch)) {
            complainMissing(maid, node, ingredient);
            return;
        }

        BlockPos best = fetch.target().pos();

        if (isCloseEnough(maid, best)) {
            take(level, maid, best, node);
            ChatBubbleUtil.removeChatBubble(maid);
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
        ExecutionNode node = pendingNode.remove(maid);
        BlockPos target = getTargetPos(maid);

        if (node != null && target != null && isCloseEnough(maid, target)
                && node.getResolution() instanceof Resolution.Fetch) {
            take(level, maid, target, node);
        }

        MemoryUtil.removeTarget(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private void take(ServerLevel level, EntityMaid maid, BlockPos pos, ExecutionNode node) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        try {
            IItemHandler maidInv = maid.getAvailableInv(false);
            WorldContext world = new WorldContext(MaidWorldView.of(maidInv), LevelRecipeLookup.of(level));
            int count = node.calculateRequiredCount(world);
            if (count > 0) {
                List<ItemStack> existed = ChefScheduler.getExistedInputs(level, maid, node.getParent());
                count -= InvUtil.count(existed, node.getIngredient());
            }

            if (count <= 0) {
                node.setProgress(Progress.DONE);
                return;
            }

            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) return;

            maid.swing(InteractionHand.OFF_HAND);
            if (InvUtil.take(level, pos, storage, maidInv, node.getIngredient(), count)) {
                node.setProgress(Progress.DONE);
            }
        } finally {
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
        }
    }

    private void complainMissing(EntityMaid maid, ExecutionNode node, Ingredient ingredient) {
        if (!node.isLeaf()) return;

        ChatBubbleUtil.setTextChatBubble(maid, Component.literal(
                "主人,我缺少" + ingredient.getItems()[0].getDisplayName().getString() + "!"));
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