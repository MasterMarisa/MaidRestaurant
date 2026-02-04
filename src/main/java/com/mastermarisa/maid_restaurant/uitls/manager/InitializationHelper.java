package com.mastermarisa.maid_restaurant.uitls.manager;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class InitializationHelper {
    private static final List<PreparableReloadListener> listeners;
    private static final List<Consumer<ServerAboutToStartEvent>> serverAboutToStartActions;
    private static MinecraftServer serverCache;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        setServerCache(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        serverCache = null;
    }

    public static @Nullable MinecraftServer getServerCache() { return serverCache; }

    public static void setServerCache(MinecraftServer server) { serverCache = server; }

    public static void accept(ServerAboutToStartEvent event) {
        for (Consumer<ServerAboutToStartEvent> action : serverAboutToStartActions)
            action.accept(event);
    }

    public static void register(AddReloadListenerEvent event) {
        for (PreparableReloadListener listener : listeners)
            event.addListener(listener);
    }

    public static void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }

    public static void addAction(Consumer<ServerAboutToStartEvent> action) {
        serverAboutToStartActions.add(action);
    }

    static {
        listeners = new ArrayList<>();
        serverAboutToStartActions = new ArrayList<>();
    }
}
