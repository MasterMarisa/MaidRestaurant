package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.uitls.manager.InitializationHelper;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public class OnAddReloadListeners {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        InitializationHelper.register(event);
    }
}
