package com.mastermarisa.maid_restaurant.core.zone;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.uitls.SerializerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractZone implements Iterable<BlockPos>, INBTSerializable<CompoundTag> {
    public abstract boolean contains(BlockPos pos);

    public static final SerializerRegistry<AbstractZone> REGISTRY = new SerializerRegistry<>();

    public static void registerAll() {
        REGISTRY.register(CUBOID_ZONE, CuboidZone.class, CuboidZone::fromNBT);
        REGISTRY.register(POINTSET_ZONE, PointsetZone.class, PointsetZone::fromNBT);
        REGISTRY.register(COMBINED_ZONE_WRAPPER, CombinedZoneWrapper.class, CombinedZoneWrapper::fromNBT);
    }

    public static final ResourceLocation CUBOID_ZONE = MaidRestaurant.resourceLocation("cuboid_zone");
    public static final ResourceLocation POINTSET_ZONE = MaidRestaurant.resourceLocation("pointset_zone");
    public static final ResourceLocation COMBINED_ZONE_WRAPPER = MaidRestaurant.resourceLocation("combined_zone_wrapper");
}
