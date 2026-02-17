package com.mastermarisa.maid_restaurant.client.event;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.OrderItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = MaidRestaurant.MOD_ID,value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.ORDER_ITEM.get(), MaidRestaurant.resourceLocation("order_item_state"),
                    (stack, level, entity, seed) -> OrderItem.hasRequests(stack) ? 1.0f : 0.0f);
        });
    }
}
