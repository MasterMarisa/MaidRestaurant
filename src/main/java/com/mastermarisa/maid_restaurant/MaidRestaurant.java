package com.mastermarisa.maid_restaurant;

import com.mastermarisa.maid_restaurant.data.DataGenerators;
import com.mastermarisa.maid_restaurant.events.CommonRegistry;
import com.mastermarisa.maid_restaurant.init.InitCompats;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.init.InitEvents;
import com.mastermarisa.maid_restaurant.init.InitItems;
import com.mastermarisa.maid_restaurant.network.NetWorkHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;


@Mod(MaidRestaurant.MOD_ID)
public class MaidRestaurant {
    public static final String MOD_ID = "maid_restaurant";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation resourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID,path);
    }

    public MaidRestaurant(IEventBus modEventBus, ModContainer modContainer) {
        InitEntities.register(modEventBus);
        InitItems.register(modEventBus);
        InitEvents.register(NeoForge.EVENT_BUS);
        InitCompats.register();

        modEventBus.register(CommonRegistry.class);
        modEventBus.register(NetWorkHandler.class);
        modEventBus.register(DataGenerators.class);
    }
}
