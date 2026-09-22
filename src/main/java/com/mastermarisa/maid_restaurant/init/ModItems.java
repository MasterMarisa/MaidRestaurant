package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MaidRestaurant.MOD_ID);

    RegistryObject<Item> COOKING_GUIDE = ITEMS.register("cooking_guide", () -> new CookingGuideItem(new Item.Properties()));
    RegistryObject<Item> CHEF_LICENSE = ITEMS.register("chef_license", () -> new ChefLicenseItem(new Item.Properties().stacksTo(1)));
    RegistryObject<Item> CUBOID_ZONE_DEFINITION = ITEMS.register("cuboid_zone_definition", () -> new CuboidZoneDefinitionItem(new Item.Properties()));
    RegistryObject<Item> POINTSET_ZONE_DEFINITION = ITEMS.register("pointset_zone_definition", () -> new PointsetZoneDefinitionItem(new Item.Properties()));
    RegistryObject<Item> UNBOUND_MENU = ITEMS.register("unbound_menu", () -> new UnboundMenuItem(new Item.Properties().stacksTo(1)));
    RegistryObject<Item> RESTAURANT_MENU = ITEMS.register("restaurant_menu", () -> new RestaurantMenuItem(new Item.Properties().stacksTo(1)));
}
