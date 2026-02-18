package com.mastermarisa.maid_restaurant.event;

import com.mastermarisa.maid_restaurant.utils.RequestManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class RequestDistributor {
    private static int ticksPerDistribution = 10;

    @SubscribeEvent
    public static void onLevelTickPre(LevelTickEvent.Pre event) {
        if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if(serverLevel.getGameTime() % ticksPerDistribution != 0 )return;
        RequestManager.tryDistributeRequests(serverLevel);
    }
}
