package com.mastermarisa.maid_restaurant.core.schedule;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class CookingRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";
    private static final String TAG_COUNT = "count";

    public ExecutionNode root;
    public int count;

    public CookingRequest() {
        this.root = null;
    }

    public CookingRequest(RecipeNode recipeRoot, int count) {
        this.root = ExecutionNode.fromRecipeTree(recipeRoot);
        this.count = count;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, root.getRecipeNode().serializeNBT());
        tag.putInt(TAG_COUNT, count);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            RecipeNode recipeRoot = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
            root = ExecutionNode.fromRecipeTree(recipeRoot);
        }
        if (tag.contains(TAG_COUNT)) {
            this.count = tag.getInt(TAG_COUNT);
        }
    }

    public static CookingRequest fromNBT(CompoundTag tag) {
        CookingRequest context = new CookingRequest();
        context.deserializeNBT(tag);
        return context;
    }
}
