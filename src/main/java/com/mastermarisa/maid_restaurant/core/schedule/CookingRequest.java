package com.mastermarisa.maid_restaurant.core.schedule;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class CookingRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";

    private ExecutionNode root;

    public CookingRequest() {
        this.root = null;
    }

    public CookingRequest(RecipeNode recipeRoot) {
        this.root = ExecutionNode.fromRecipeTree(recipeRoot);
    }

    public ExecutionNode getRoot() {
        return root;
    }

    public void setRoot(ExecutionNode root) { this.root = root; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, root.getRecipeNode().serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            RecipeNode recipeRoot = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
            root = ExecutionNode.fromRecipeTree(recipeRoot);
        }
    }

    public static CookingRequest fromNBT(CompoundTag tag) {
        CookingRequest context = new CookingRequest();
        context.deserializeNBT(tag);
        return context;
    }
}
