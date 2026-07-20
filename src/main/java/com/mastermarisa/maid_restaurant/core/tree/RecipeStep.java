package com.mastermarisa.maid_restaurant.core.tree;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record RecipeStep(String capabilityID, ResourceLocation recipeId) {
    private static final String TAG_CAPABILITY_ID = "capability_id";
    private static final String TAG_RECIPE_ID = "recipe_id";

    public RecipeStep copy() {
        return new RecipeStep(capabilityID, recipeId);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_CAPABILITY_ID, capabilityID);
        tag.putString(TAG_RECIPE_ID, recipeId.toString());
        return tag;
    }

    @Nullable
    public static RecipeStep fromNBT(CompoundTag tag) {
        if (!tag.contains(TAG_CAPABILITY_ID) || !tag.contains(TAG_RECIPE_ID)) {
            return null;
        }
        String capabilityID = tag.getString(TAG_CAPABILITY_ID);
        ResourceLocation recipeId = ResourceLocation.tryParse(tag.getString(TAG_RECIPE_ID));
        if (recipeId == null) {
            return null;
        }
        return new RecipeStep(capabilityID, recipeId);
    }
}
