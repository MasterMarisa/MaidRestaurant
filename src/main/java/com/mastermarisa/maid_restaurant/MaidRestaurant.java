package com.mastermarisa.maid_restaurant;

import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.KaleidoscopeCookeryCompat;
import com.mastermarisa.maid_restaurant.init.ModContainers;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MaidRestaurant.MOD_ID)
public class MaidRestaurant {
    public static final String MOD_ID = "maid_restaurant";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation modLoc(String path) {
        return new ResourceLocation(MOD_ID,path);
    }

    public MaidRestaurant() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.MEMORY_MODULE_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModContainers.CONTAINER_TYPES.register(modEventBus);

        KaleidoscopeCookeryCompat.registerCapabilities();
    }
}
