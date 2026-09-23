package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModCreativeTabs {
    DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaidRestaurant.MOD_ID);

    RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.maid_restaurant.main.name"))
            .icon(ModItems.CHEF_LICENSE.get()::getDefaultInstance)
            .displayItems((par, output) -> {
                output.accept(ModItems.CHEF_LICENSE.get());
                output.accept(ModItems.POINTSET_ZONE_DEFINITION.get());
                output.accept(ModItems.CUBOID_ZONE_DEFINITION.get());
                output.accept(ModItems.COOKING_GUIDE.get());
                output.accept(ModItems.UNBOUND_MENU.get());
                output.accept(ModItems.CREATIVE_CRATE.get());
            }).build());
}
