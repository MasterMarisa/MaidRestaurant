package com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery;

import com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.capability.*;
import com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.storage.FruitBasketStorage;
import com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.storage.TableStorage;
import net.minecraftforge.fml.ModList;

public class KaleidoscopeCookeryCompat {
    public static final boolean LOADED = ModList.get().isLoaded("kaleidoscope_cookery");

    public static void registerCapabilities() {
        if (LOADED) {
            PotCapability.register();
            StockpotCapability.register();
            SteamerCapability.register();
            TeapotCapability.register();
            ChoppingBoardCapability.register();
        }
    }

    public static void registerStorages() {
        if (LOADED) {
            TableStorage.register();
            FruitBasketStorage.register();
        }
    }
}
