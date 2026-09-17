package com.mastermarisa.maid_restaurant.init.registry;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CraftingTableCapability;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.integration.ae2.AE2Compat;
import com.mastermarisa.maid_restaurant.integration.bakeries.BakeriesCompat;
import com.mastermarisa.maid_restaurant.integration.create.CreateCompat;
import com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.KaleidoscopeCookeryCompat;
import com.mastermarisa.maid_restaurant.integration.kaleidoscope_tavern.KaleidoscopeTavernCompat;
import com.mastermarisa.maid_restaurant.integration.rs.RSCompat;
import com.mastermarisa.maid_restaurant.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.storage.CommonStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
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

    private static void registerSerializers() {
        AbstractZone.registerAll();
        RequestBus.registerSerializers();
    }

    private static void registerCapabilities() {
        CapabilityRegistry.register(new CraftingTableCapability());
        KaleidoscopeCookeryCompat.registerCapabilities();
        KaleidoscopeTavernCompat.registerCapabilities();
        BakeriesCompat.registerCapabilities();
    }

    private static void registerStorages() {
        StorageRegistry.register(new CommonStorage());
        KaleidoscopeCookeryCompat.registerStorages();
        AE2Compat.registerStorages();
        RSCompat.registerStorages();
        CreateCompat.registerStorages();
    }
}
