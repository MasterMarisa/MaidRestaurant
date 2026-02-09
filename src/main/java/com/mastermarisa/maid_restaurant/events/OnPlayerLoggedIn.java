package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class OnPlayerLoggedIn {
    @SubscribeEvent
    public static void onEntityJoin(PlayerEvent.PlayerLoggedInEvent event) {
        CookTaskManager.register();
    }
}
