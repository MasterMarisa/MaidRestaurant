package com.mastermarisa.maid_restaurant.integration.ae2;

import com.mastermarisa.maid_restaurant.integration.ae2.storage.AE2Storage;
import net.minecraftforge.fml.ModList;

public class AE2Compat {
    public static final boolean LOADED = ModList.get().isLoaded("ae2");

    public static void registerStorages() {
        if (LOADED) {
            AE2Storage.register();
        }
    }
}
