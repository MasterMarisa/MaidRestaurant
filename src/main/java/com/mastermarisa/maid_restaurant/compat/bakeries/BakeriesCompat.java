package com.mastermarisa.maid_restaurant.compat.bakeries;

import net.neoforged.fml.ModList;

public class BakeriesCompat {
    public static final boolean LOADED = ModList.get().isLoaded("bakeries");

    public static void register() {
        if (LOADED) {
            OvenCookTask.register();
            BlenderCookTask.register();
            ToasterCookTask.register();
            GlassDrinkCupCookTask.register();
        }
    }
}
