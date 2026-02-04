package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.compat.farmersdelight.FarmersDelightCompat;

public interface InitCompats {
    static void register() {
        FarmersDelightCompat.register();
    }
}
