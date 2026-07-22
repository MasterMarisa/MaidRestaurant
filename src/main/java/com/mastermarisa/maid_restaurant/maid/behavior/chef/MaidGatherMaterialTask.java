package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import com.mastermarisa.maid_restaurant.maid.behavior.base.CheckRateHelper;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.ChatBubbleUtil;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.mastermarisa.maid_restaurant.uitls.MemoryUtil;
import com.mastermarisa.maid_restaurant.uitls.PosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class MaidGatherMaterialTask extends MaidCheckRateTask {
    public static final String UID = "GatherMaterial";

    private final float movementSpeed;
    private final double closeEnoughDistSqr;

    public MaidGatherMaterialTask(int maxInterval, float movementSpeed, double closeEnoughDist) {
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

        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.NEED_MATERIALS);
        if (node == null) {
            return false;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        List<ItemStack> existedInputs = ChefScheduler.getExistedInputs(level, maid, node.getParent());

        if (InvUtil.contains(maidInv, existedInputs, node.getIngredient(), node.getCount())) {
            node.setState(NodeState.DONE);
            if (node.getParent() != null) {
                node.getParent().computeState();
            }
            CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 1);
            return false;
        }
        return searchStorage(level, maid, node);
    }

    @Override
    protected void start(ServerLevel pLevel, EntityMaid pEntity, long pGameTime) {
        MaidRestaurant.LOGGER.debug("MaidGatherMaterialTask - START");
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return MemoryUtil.isTarget(maid, TargetType.GATHER_MATERIAL)
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
        MemoryUtil.removeTargetIfMatch(maid, TargetType.GATHER_MATERIAL);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.setDeltaMovement(Vec3.ZERO);
    }

    private boolean searchStorage(ServerLevel level, EntityMaid maid, ExecutionNode node) {
        AbstractZone zone = ChefScheduler.getStorageZone(maid);
        if (zone == null) {
            return false;
        }

        RecipeNode recipeNode = node.getRecipeNode();
        Ingredient ingredient = recipeNode.getIngredient();

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos center = maid.blockPosition();

        for (BlockPos pos : zone) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage != null && storage.count(level, pos, ingredient) > 0) {
                double dist = pos.distSqr(center);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos;
                }
            }
        }

        if (best != null) {
            ChatBubbleUtil.removeChatBubble(maid);
            MemoryUtil.setTarget(maid, new BlockPosTracker(best), TargetType.GATHER_MATERIAL);
            MemoryUtil.setWalkAndLookTargetMemories(maid, best, best, movementSpeed, 0);
            return true;
        }

        ChatBubbleUtil.setTextChatBubble(maid, Component.literal("主人,我缺少" + ingredient.getItems()[0].getDisplayName().getString() + "!"));
        return false;
    }

    private void acceptStorage(ServerLevel level, EntityMaid maid, BlockPos pos) {
        ExecutionNode node = ChefScheduler.findNode(level, maid, NodeState.NEED_MATERIALS);
        if (node == null || !node.isLeaf()) return;

        IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
        if (storage == null) return;

        IItemHandler maidInv = maid.getAvailableInv(false);
        int required = node.getCount() - InvUtil.count(maidInv, node.getIngredient());

        maid.swing(InteractionHand.OFF_HAND);
        if (InvUtil.tryTake(level, pos, storage, maidInv, node.getIngredient(), required)) {
            node.setState(NodeState.DONE);
        }

        ExecutionNode parent = node.getParent();
        if (parent != null) {
            parent.computeState();
        }

        CheckRateHelper.setRemainingTicks(maid.getUUID(), UID, 5);
    }
}

