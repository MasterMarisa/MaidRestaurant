package com.mastermarisa.maid_restaurant.compat.farmersdelight;

import net.neoforged.fml.ModList;

public class FarmersDelightCompat {
    public static final boolean LOADED = ModList.get().isLoaded("farmersdelight");

    public static void register() {
        if (LOADED) {
            CookingPotCookTask.register();
        }
    }
}
