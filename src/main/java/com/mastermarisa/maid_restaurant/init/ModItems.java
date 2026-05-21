package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.item.CuboidZoneDefinitionItem;
import com.mastermarisa.maid_restaurant.item.PointSetZoneDefinitionItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MaidRestaurant.MOD_ID);

    RegistryObject<Item> COOKING_GUIDE = ITEMS.register("cooking_guide", () -> new CookingGuideItem(new Item.Properties()));
    RegistryObject<Item> CUBOID_ZONE_DEFINITION = ITEMS.register("cuboid_zone_definition", () -> new CuboidZoneDefinitionItem(new Item.Properties()));
    RegistryObject<Item> POINT_SET_ZONE_DEFINITION = ITEMS.register("point_set_zone_definition", () -> new PointSetZoneDefinitionItem(new Item.Properties()));
}
