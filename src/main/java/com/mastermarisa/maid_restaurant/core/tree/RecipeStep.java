package com.mastermarisa.maid_restaurant.core.tree;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class RecipeStep implements INBTSerializable<CompoundTag> {
    private static final String TAG_CAPABILITY_UID = "capability_uid";
    private static final String TAG_RECIPE_ID = "recipe_id";

    private String capabilityUID;
    @Nullable
    private ResourceLocation recipeId;

    public RecipeStep() {
        this.capabilityUID = "";
        this.recipeId = null;
    }

    public RecipeStep(String capabilityUID, @Nullable ResourceLocation recipeId) {
        this.capabilityUID = capabilityUID;
        this.recipeId = recipeId;
    }

    public String getCapabilityUID() {
        return capabilityUID;
    }

    public void setCapabilityUID(String capabilityUID) {
        this.capabilityUID = capabilityUID;
    }

    @Nullable
    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@Nullable ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public RecipeStep copy() {
        return new RecipeStep(capabilityUID, recipeId);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_CAPABILITY_UID, capabilityUID);
        if (recipeId != null) {
            tag.putString(TAG_RECIPE_ID, recipeId.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_CAPABILITY_UID)) {
            capabilityUID = tag.getString(TAG_CAPABILITY_UID);
        }
        if (tag.contains(TAG_RECIPE_ID)) {
            recipeId = ResourceLocation.tryParse(tag.getString(TAG_RECIPE_ID));
        }
    }

    public static RecipeStep fromNBT(CompoundTag tag) {
        RecipeStep step = new RecipeStep();
        step.deserializeNBT(tag);
        return step;
    }
}
