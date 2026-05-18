package com.mastermarisa.maid_restaurant.init.tag;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface TagMod {
    TagKey<Block> STORAGE_BLOCK = blockTag("storage_block");

    static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, MaidRestaurant.resourceLocation(name));
    }

    static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, MaidRestaurant.resourceLocation(name));
    }

    static TagKey<EntityType<?>> entityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, MaidRestaurant.resourceLocation(name));
    }

    static TagKey<DamageType> damageTypeTag(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, MaidRestaurant.resourceLocation(name));
    }
}
