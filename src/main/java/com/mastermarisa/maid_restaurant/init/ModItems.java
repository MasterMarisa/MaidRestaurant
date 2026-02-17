package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.item.OrderItem;
import com.mastermarisa.maid_restaurant.item.OrderMenuItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaidRestaurant.MOD_ID);

    public static final DeferredItem<Item> ORDER_MENU = ITEMS.registerItem("order_menu", (properties)-> new OrderMenuItem());

    public static final DeferredItem<Item> ORDER_ITEM = ITEMS.registerItem("order_item", (properties -> new OrderItem()));

    public static void register(IEventBus mod){
        ITEMS.register(mod);
    }
}
