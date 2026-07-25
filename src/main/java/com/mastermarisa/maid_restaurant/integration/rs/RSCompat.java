package com.mastermarisa.maid_restaurant.integration.rs;

import com.mastermarisa.maid_restaurant.integration.rs.storage.RSStorage;
import net.minecraftforge.fml.ModList;

public class RSCompat {
    public static final boolean LOADED = ModList.get().isLoaded("refinedstorage");

    public static void registerStorages() {
        if (LOADED) {
            RSStorage.register();
        }
    }
}
