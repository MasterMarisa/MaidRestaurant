package com.mastermarisa.maid_restaurant;

import com.mastermarisa.maid_restaurant.init.ModEntities;
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

    public static ResourceLocation resourceLocation(String path) {
        return new ResourceLocation(MOD_ID,path);
    }

    public MaidRestaurant() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
