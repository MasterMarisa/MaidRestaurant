package com.mastermarisa.maid_restaurant.data.task_data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.zone.CombinedZoneWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class ChefInformation implements INBTSerializable<CompoundTag> {
    private static final String TAG_WORK_ZONE = "work_zone";
    private static final String TAG_STORAGE_ZONE = "storage_zone";
    private static final String TAG_PREP_ZONE = "prep_zone";

    @Nullable
    private CombinedZoneWrapper workZone;
    @Nullable
    private CombinedZoneWrapper storageZone;
    @Nullable
    private CombinedZoneWrapper prepZone;

    public ChefInformation() {}

    public ChefInformation(@Nullable CombinedZoneWrapper workZone,
                           @Nullable CombinedZoneWrapper storageZone,
                           @Nullable CombinedZoneWrapper prepZone) {
        this.workZone = workZone;
        this.storageZone = storageZone;
        this.prepZone = prepZone;
    }

    @Nullable
    public CombinedZoneWrapper getWorkZone() {
        return workZone;
    }

    @Nullable
    public CombinedZoneWrapper getStorageZone() {
        return storageZone;
    }

    @Nullable
    public CombinedZoneWrapper getPrepZone() { return prepZone; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (workZone != null) {
            tag.put(TAG_WORK_ZONE, workZone.serializeNBT());
        }
        if (storageZone != null) {
            tag.put(TAG_STORAGE_ZONE, storageZone.serializeNBT());
        }
        if (prepZone != null) {
            tag.put(TAG_PREP_ZONE, prepZone.serializeNBT());
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
        if (tag.contains(TAG_PREP_ZONE)) {
            prepZone = CombinedZoneWrapper.fromNBT(tag.getCompound(TAG_PREP_ZONE));
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
