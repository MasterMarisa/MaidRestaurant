package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import com.mastermarisa.maid_restaurant.uitls.manager.InitializationHelper;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.function.Consumer;

public class OnServerAboutToStart {
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event){
        MinecraftServer server = event.getServer();
        InitializationHelper.setServerCache(server);
        RecipeUtils.setRecipeManager(server.getRecipeManager());
        BlockUsageManager.reset();
        CookTaskManager.register();
        InitializationHelper.accept(event);
    }
}
