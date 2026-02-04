package com.mastermarisa.maid_restaurant.data;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        var block = generator.addProvider(event.includeServer(), new TagBlock(
                packOutput, lookupProvider, MaidRestaurant.MOD_ID, existingFileHelper
        ));

        generator.addProvider(event.includeServer(), new TagItem(
                packOutput, lookupProvider, block.contentsGetter(), existingFileHelper
        ));
    }
}
