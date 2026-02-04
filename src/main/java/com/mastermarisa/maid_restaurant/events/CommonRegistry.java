package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.init.InitItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class CommonRegistry {
    @SubscribeEvent
    public static void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.accept((ItemLike) InitItems.ORDER_MENU.get());
        }
    }
}
