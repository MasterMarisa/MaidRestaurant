package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class RequestDistributor {
    private static long serverTickCount = 0;

    @SubscribeEvent
    public static void onLevelTickPre(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) {
            serverTickCount++;

            if (serverTickCount % 10 == 0) {
                RequestManager.trySendRequests((ServerLevel) event.getLevel());
            }
        }
    }
}
