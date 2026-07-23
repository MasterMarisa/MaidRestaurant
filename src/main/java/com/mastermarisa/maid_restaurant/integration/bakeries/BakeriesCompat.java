package com.mastermarisa.maid_restaurant.integration.bakeries;

import com.mastermarisa.maid_restaurant.integration.bakeries.capability.OvenCapability;
import net.minecraftforge.fml.ModList;

public class BakeriesCompat {
    public static final boolean LOADED = ModList.get().isLoaded("bakeries");

    public static void registerCapabilities() {
        if (LOADED) {
            OvenCapability.register();
        }
    }
}
