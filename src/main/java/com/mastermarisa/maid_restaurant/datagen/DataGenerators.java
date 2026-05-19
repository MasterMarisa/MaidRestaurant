package com.mastermarisa.maid_restaurant.datagen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.datagen.model.ItemModelGenerator;
import com.mastermarisa.maid_restaurant.datagen.tag.TagBlock;
import com.mastermarisa.maid_restaurant.datagen.tag.TagItem;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var vanillaPack = generator.getVanillaPack(true);
        var helper = event.getExistingFileHelper();
        var pack = generator.getPackOutput();

        var block = vanillaPack.addProvider(packOutput -> new TagBlock(packOutput, registries, helper));
        vanillaPack.addProvider(packOutput -> new TagItem(packOutput, registries, block.contentsGetter(), helper));

        generator.addProvider(event.includeClient(), new ItemModelGenerator(pack, helper));
    }
}
