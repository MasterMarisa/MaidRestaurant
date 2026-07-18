package com.mastermarisa.maid_restaurant.init.registry;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.KaleidoscopeCookeryCompat;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CraftingTableCapability;
import com.mastermarisa.maid_restaurant.core.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.core.storage.CommonStorage;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonRegistry {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonRegistry::registerSerializers);
        event.enqueueWork(CommonRegistry::registerCapabilities);
        event.enqueueWork(CommonRegistry::registerStorages);
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CHEF_LICENSE.get());
            event.accept(ModItems.CUBOID_ZONE_DEFINITION.get());
            event.accept(ModItems.POINTSET_ZONE_DEFINITION.get());
            event.accept(ModItems.COOKING_GUIDE.get());
        }
    }

    private static void registerSerializers() {
        AbstractZone.registerAll();
        RequestBus.registerSerializers();
    }

    private static void registerCapabilities() {
        CapabilityRegistry.register(new CraftingTableCapability());
        KaleidoscopeCookeryCompat.registerCapabilities();
    }

    private static void registerStorages() {
        StorageRegistry.register(new CommonStorage());
        KaleidoscopeCookeryCompat.registerStorages();
    }
}
