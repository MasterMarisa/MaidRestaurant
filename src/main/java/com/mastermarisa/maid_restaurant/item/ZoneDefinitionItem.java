package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class ZoneDefinitionItem extends Item {
    private static final String TAG_ZONE = "zone";

    public ZoneDefinitionItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static AbstractZone getZone(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_ZONE)) {
            return AbstractZone.REGISTRY.deserialize(tag.getCompound(TAG_ZONE));
        }
        return null;
    }

    public static void setZone(ItemStack itemStack, AbstractZone zone) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put(TAG_ZONE, AbstractZone.REGISTRY.serialize(zone));
    }
}
