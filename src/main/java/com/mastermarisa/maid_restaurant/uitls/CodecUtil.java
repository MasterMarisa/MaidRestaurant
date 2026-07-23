package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class CodecUtil {
    public static <T> CompoundTag serialize(T obj, Codec<T> codec) {
        return (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, obj)
                .resultOrPartial(MaidRestaurant.LOGGER::error)
                .orElseThrow();
    }

    public static <T> T deserialize(CompoundTag tag, Codec<T> codec) {
        return codec.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(MaidRestaurant.LOGGER::error)
                .orElseThrow();
    }
}
