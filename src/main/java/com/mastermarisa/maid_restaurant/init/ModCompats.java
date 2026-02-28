package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.compat.bakeries.BakeriesCompat;
import com.mastermarisa.maid_restaurant.compat.farmersdelight.FarmersDelightCompat;

public class ModCompats {
    public static void register() {
        FarmersDelightCompat.register();
        BakeriesCompat.register();
    }
}
