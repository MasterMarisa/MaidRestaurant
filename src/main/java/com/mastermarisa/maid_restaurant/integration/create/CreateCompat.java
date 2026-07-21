package com.mastermarisa.maid_restaurant.integration.create;

import com.mastermarisa.maid_restaurant.integration.create.storage.CreativeCrateStorage;
import net.minecraftforge.fml.ModList;

public class CreateCompat {
    public static final boolean LOADED = ModList.get().isLoaded("create");

    public static void registerStorages() {
        if (LOADED) {
            CreativeCrateStorage.register();
        }
    }
}
