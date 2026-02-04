package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EncodeUtils {
    public static ResourceLocation encode(Item item){
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation encode(ItemStack stack){
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static Item decode(String key){ return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key)); }

    public static Item decode(ResourceLocation key){
        return BuiltInRegistries.ITEM.get(key);
    }
}
