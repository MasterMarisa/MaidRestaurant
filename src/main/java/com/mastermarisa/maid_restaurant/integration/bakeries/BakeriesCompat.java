package com.mastermarisa.maid_restaurant.integration.bakeries;

import com.mastermarisa.maid_restaurant.integration.bakeries.capability.BlenderCapability;
import com.mastermarisa.maid_restaurant.integration.bakeries.capability.DoughCraftingTableCapability;
import com.mastermarisa.maid_restaurant.integration.bakeries.capability.FermentationBoxCapability;
import com.mastermarisa.maid_restaurant.integration.bakeries.capability.OvenCapability;
import net.minecraftforge.fml.ModList;

public class BakeriesCompat {
    public static final boolean LOADED = ModList.get().isLoaded("bakeries");

    public static void registerCapabilities() {
        if (LOADED) {
            DoughCraftingTableCapability.register();
            BlenderCapability.register();
            FermentationBoxCapability.register();
            OvenCapability.register();
        }
    }
}
