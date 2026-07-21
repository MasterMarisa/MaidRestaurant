package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery;

import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability.ChoppingBoardCapability;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability.PotCapability;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability.SteamerCapability;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability.StockpotCapability;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.storage.FruitBasketStorage;
import com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.storage.TableStorage;
import net.minecraftforge.fml.ModList;

public class KaleidoscopeCookeryCompat {
    public static final boolean LOADED = ModList.get().isLoaded("kaleidoscope_cookery");

    public static void registerCapabilities() {
        if (LOADED) {
            ChoppingBoardCapability.register();
            PotCapability.register();
            StockpotCapability.register();
            SteamerCapability.register();
        }
    }

    public static void registerStorages() {
        if (LOADED) {
            TableStorage.register();
            FruitBasketStorage.register();
        }
    }
}
