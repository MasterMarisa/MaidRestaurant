package com.mastermarisa.maid_restaurant.core.schedule;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CookingRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";
    private static final String TAG_TARGET_POSITIONS = "target_positions";

    private ExecutionNode root;
    private List<Long> targetPositions;

    public CookingRequest() {
        this.root = null;
        this.targetPositions = new ArrayList<>();
    }

    public CookingRequest(RecipeNode recipeRoot) {
        this.root = ExecutionNode.fromRecipeTree(recipeRoot);
        this.targetPositions = new ArrayList<>();
    }

    public ExecutionNode getRoot() {
        return root;
    }

    public void setRoot(ExecutionNode root) { this.root = root; }

    public List<Long> getTargetPositions() {
        return targetPositions;
    }

    public void setTargetPositions(List<Long> targetPositions) {
        this.targetPositions = new ArrayList<>(targetPositions);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, root.getRecipeNode().serializeNBT());
        tag.putLongArray(TAG_TARGET_POSITIONS, targetPositions);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            RecipeNode recipeRoot = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
            root = ExecutionNode.fromRecipeTree(recipeRoot);
            root.computeState();
        }
        if (tag.contains(TAG_TARGET_POSITIONS)) {
            targetPositions = Arrays.stream(tag.getLongArray(TAG_TARGET_POSITIONS)).boxed().toList();
        }
    }

    public static CookingRequest fromNBT(CompoundTag tag) {
        CookingRequest context = new CookingRequest();
        context.deserializeNBT(tag);
        return context;
    }
}
