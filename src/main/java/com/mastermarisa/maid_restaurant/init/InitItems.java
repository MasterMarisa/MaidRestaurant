package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.item.OrderMenuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface InitItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaidRestaurant.MOD_ID);

    DeferredItem<Item> ORDER_MENU = ITEMS.registerItem("order_menu", (properties)-> new OrderMenuItem());

    static void register(IEventBus mod){
        ITEMS.register(mod);
    }
}
