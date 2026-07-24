package com.mastermarisa.maid_restaurant.integration.kaleidoscope_tavern;

import com.mastermarisa.maid_restaurant.integration.kaleidoscope_tavern.capability.ShakerCapability;
import net.minecraftforge.fml.ModList;

public class KaleidoscopeTavernCompat {
    public static final boolean LOADED = ModList.get().isLoaded("kaleidoscope_tavern");

    public static void registerCapabilities() {
        if (LOADED) {
            ShakerCapability.register();
        }
    }
}
