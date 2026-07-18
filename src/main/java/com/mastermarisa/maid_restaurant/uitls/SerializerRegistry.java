package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class SerializerRegistry<T extends INBTSerializable<CompoundTag>> {
    private final Map<ResourceLocation, Function<CompoundTag, ? extends T>> deserializerByKey = new LinkedHashMap<>();
    public final Map<Class<? extends T>, ResourceLocation> keyByClass = new IdentityHashMap<>();

    public <U extends T> void register(
            ResourceLocation key,
            Class<U> type,
            Function<CompoundTag, U> deserializer) {
        deserializerByKey.put(key, deserializer);
        keyByClass.put(type, key);
    }

    public CompoundTag serialize(T value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", keyByClass.get(value.getClass()).toString());
        tag.put("data", value.serializeNBT());
        return tag;
    }

    public T deserialize(CompoundTag tag) {
        ResourceLocation type = new ResourceLocation(tag.getString("type"));
        return deserializerByKey.get(type).apply(tag.getCompound("data"));
    }
}
