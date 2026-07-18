package com.mastermarisa.maid_restaurant.core.schedule;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.zone.CombinedZoneWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class ChefInformation implements INBTSerializable<CompoundTag> {
    private static final String TAG_WORK_ZONE = "work_zone";
    private static final String TAG_STORAGE_ZONE = "storage_zone";

    @Nullable
    private CombinedZoneWrapper workZone;
    @Nullable
    private CombinedZoneWrapper storageZone;

    public ChefInformation() {}

    public ChefInformation(@Nullable CombinedZoneWrapper workZone, @Nullable CombinedZoneWrapper storageZone) {
        this.workZone = workZone;
        this.storageZone = storageZone;
    }

    @Nullable
    public CombinedZoneWrapper getWorkZone() {
        return workZone;
    }

    @Nullable
    public CombinedZoneWrapper getStorageZone() {
        return storageZone;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (workZone != null) {
            tag.put(TAG_WORK_ZONE, workZone.serializeNBT());
        }
        if (storageZone != null) {
            tag.put(TAG_STORAGE_ZONE, storageZone.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_WORK_ZONE)) {
            workZone = CombinedZoneWrapper.fromNBT(tag.getCompound(TAG_WORK_ZONE));
        }
        if (tag.contains(TAG_STORAGE_ZONE)) {
            storageZone = CombinedZoneWrapper.fromNBT(tag.getCompound(TAG_STORAGE_ZONE));
        }
    }

    public static ChefInformation fromNBT(CompoundTag tag) {
        ChefInformation info = new ChefInformation();
        info.deserializeNBT(tag);
        return info;
    }

    public static class DataKey implements TaskDataKey<ChefInformation> {
        private static final ResourceLocation KEY = MaidRestaurant.modLoc("chef_info");

        @Override
        public ResourceLocation getKey() { return KEY; }

        @Override
        public CompoundTag writeSaveData(ChefInformation chefInfo) {
            return chefInfo.serializeNBT();
        }

        @Override
        public ChefInformation readSaveData(CompoundTag compoundTag) {
            return ChefInformation.fromNBT(compoundTag);
        }
    }
}
