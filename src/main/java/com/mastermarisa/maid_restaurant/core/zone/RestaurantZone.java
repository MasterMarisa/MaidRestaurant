package com.mastermarisa.maid_restaurant.core.zone;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class RestaurantZone implements INBTSerializable<CompoundTag> {
    private static final String TAG_MIN = "min";
    private static final String TAG_MAX = "max";

    private BlockPos min;
    private BlockPos max;

    public RestaurantZone() {
        this.min = BlockPos.ZERO;
        this.max = BlockPos.ZERO;
    }

    public RestaurantZone(BlockPos p1, BlockPos p2) {
        this.min = new BlockPos(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
        );
        this.max = new BlockPos(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())
        );
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    public BlockPos getCenter() {
        return new BlockPos(
                (min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2
        );
    }

    public boolean contains(BlockPos pos) {
        return contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }

    public boolean isValid() {
        return min.asLong() != max.asLong();
    }

    public int getWidth() {
        return max.getX() - min.getX();
    }

    public int getHeight() {
        return max.getY() - min.getY();
    }

    public int getDepth() {
        return max.getZ() - min.getZ();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_MIN, min.asLong());
        tag.putLong(TAG_MAX, max.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_MIN)) {
            min = BlockPos.of(tag.getLong(TAG_MIN));
        }
        if (tag.contains(TAG_MAX)) {
            max = BlockPos.of(tag.getLong(TAG_MAX));
        }
    }

    @Override
    public String toString() {
        return "RestaurantZone[min=" + min.toShortString() + ", max=" + max.toShortString() + "]";
    }

    public static class DATA_KEY implements TaskDataKey<RestaurantZone> {
        @Override
        public ResourceLocation getKey() {
            return MaidRestaurant.resourceLocation("restaurant_zone");
        }

        @Override
        public CompoundTag writeSaveData(RestaurantZone zone) {
            return zone.serializeNBT();
        }

        @Override
        public RestaurantZone readSaveData(CompoundTag tag) {
            RestaurantZone zone = new RestaurantZone();
            zone.deserializeNBT(tag);
            return zone;
        }
    }

    @Nullable
    public static RestaurantZone getZone(EntityMaid maid) {
        RestaurantZone zone = maid.getData(ModTaskDataKeys.RESTAURANT_ZONE);
        if (zone != null && zone.isValid()) return zone;
        return null;
    }
}
