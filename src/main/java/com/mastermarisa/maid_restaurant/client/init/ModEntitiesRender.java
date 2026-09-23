package com.mastermarisa.maid_restaurant.client.init;

import com.mastermarisa.maid_restaurant.client.model.OrderBellModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        value = {Dist.CLIENT},
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModEntitiesRender {
    public ModEntitiesRender() {
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {

    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OrderBellModel.LAYER_LOCATION, OrderBellModel::createBodyLayer);
    }
}
