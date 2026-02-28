package com.mastermarisa.maid_restaurant;

import com.mastermarisa.maid_restaurant.config.RestaurantConfig;
import com.mastermarisa.maid_restaurant.data.DataGenerators;
import com.mastermarisa.maid_restaurant.event.BlockSelector;
import com.mastermarisa.maid_restaurant.init.*;
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
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModCompats.register();
        RestaurantConfig.register(modContainer);
        ModTrigger.register(modEventBus);

        modEventBus.register(DataGenerators.class);
    }
}
