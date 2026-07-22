package com.mastermarisa.maid_restaurant.data.request;

import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class CookingRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";
    private static final String TAG_BOUND_REQUEST = "bound_request";

    public ExecutionNode root;
    @Nullable
    public ServeRequest boundRequest;

    public CookingRequest() {}

    public CookingRequest(RecipeNode recipeRoot) {
        this.root = ExecutionNode.fromRecipeTree(recipeRoot);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, this.root.getRecipeNode().serializeNBT());
        if (this.boundRequest != null) {
            tag.put(TAG_BOUND_REQUEST, this.boundRequest.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            RecipeNode recipeRoot = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
            this.root = ExecutionNode.fromRecipeTree(recipeRoot);
        }
        if (tag.contains(TAG_BOUND_REQUEST)) {
            this.boundRequest = ServeRequest.fromNBT(tag.getCompound(TAG_BOUND_REQUEST));
        }
    }

    public static CookingRequest fromNBT(CompoundTag tag) {
        CookingRequest request = new CookingRequest();
        request.deserializeNBT(tag);
        return request;
    }
}
