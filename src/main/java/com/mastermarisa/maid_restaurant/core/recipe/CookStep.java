package com.mastermarisa.maid_restaurant.core.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class CookStep implements INBTSerializable<CompoundTag> {
    public static final String RECIPE_ID = "recipe_id";
    private static final String TAG_CAPABILITY_UID = "capability_uid";
    private static final String TAG_PARAMS = "params";

    private String capabilityUID;
    private CompoundTag params;

    public CookStep() {
        this.capabilityUID = "";
        this.params = new CompoundTag();
    }

    public CookStep(String capabilityUID, CompoundTag params) {
        this.capabilityUID = capabilityUID;
        this.params = params.copy();
    }

    public String getCapabilityUID() {
        return capabilityUID;
    }

    public void setCapabilityUID(String capabilityUID) {
        this.capabilityUID = capabilityUID;
    }

    public CompoundTag getParams() {
        return params;
    }

    public void setParams(CompoundTag params) {
        this.params = params.copy();
    }

    public CookStep copy() {
        return new CookStep(capabilityUID, params.copy());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_CAPABILITY_UID, capabilityUID);
        tag.put(TAG_PARAMS, params.copy());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        capabilityUID = tag.getString(TAG_CAPABILITY_UID);
        params = tag.getCompound(TAG_PARAMS);
    }

    public static CookStep fromNBT(CompoundTag tag) {
        CookStep step = new CookStep();
        step.deserializeNBT(tag);
        return step;
    }
}
