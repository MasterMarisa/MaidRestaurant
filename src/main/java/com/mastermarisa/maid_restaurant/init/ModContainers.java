package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.inventory.container.ChefLicenseContainer;
import com.mastermarisa.maid_restaurant.inventory.container.WaiterLicenseContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModContainers {
    DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MaidRestaurant.MOD_ID);

    RegistryObject<MenuType<ChefLicenseContainer>> CHEF_LICENSE_CONTAINER = CONTAINER_TYPES.register("chef_license_container", () -> ChefLicenseContainer.TYPE);
    RegistryObject<MenuType<WaiterLicenseContainer>> WAITER_LICENSE_CONTAINER = CONTAINER_TYPES.register("waiter_license_container", () -> WaiterLicenseContainer.TYPE);
}
