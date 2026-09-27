package com.mastermarisa.maid_restaurant.core.request;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class RequestBusTicker {
    private static final List<Function<ServerLevel, RequestBus<?>>> BUSES = List.of(
            CookingRequestBus::getInstance,
            ServingRequestBus::getInstance
    );

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        // 每 293 tick（约 15 秒）清理一次
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getGameTime() % 293 != 0) continue;

            for (var factory : BUSES) {
                factory.apply(level).cleanup(level);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            for (var factory : BUSES) {
                factory.apply(level).unload(level);
            }
        }
    }
}
