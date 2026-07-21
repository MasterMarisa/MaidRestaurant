package com.mastermarisa.maid_restaurant.data.task_data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public class WorkBlockCache implements INBTSerializable<CompoundTag> {
    private BlockPos pos;
    private String capabilityUID;

    public WorkBlockCache() {}

    public WorkBlockCache(BlockPos pos, String capabilityUID) {
        this.pos = pos;
        this.capabilityUID = capabilityUID;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getCapabilityUID() {
        return capabilityUID;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", pos.asLong());
        tag.putString("capability_uid", capabilityUID);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("pos")) {
            this.pos = BlockPos.of(tag.getLong("pos"));
        }
        if (tag.contains("capability_uid")) {
            this.capabilityUID = tag.getString("capability_uid");
        }
    }

    public static WorkBlockCache fromNBT(CompoundTag tag) {
        WorkBlockCache cache = new WorkBlockCache();
        cache.deserializeNBT(tag);
        return cache;
    }

    public static class DataKey implements TaskDataKey<WorkBlockCache> {
        private static final ResourceLocation KEY = MaidRestaurant.modLoc("work_block_cache");

        @Override
        public ResourceLocation getKey() { return KEY; }

        @Override
        public CompoundTag writeSaveData(WorkBlockCache cache) {
            return cache.serializeNBT();
        }

        @Override
        public WorkBlockCache readSaveData(CompoundTag compoundTag) {
            return WorkBlockCache.fromNBT(compoundTag);
        }
    }
}
