package com.mastermarisa.maid_restaurant.utils;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.cooktask.PotCookTask;
import com.mastermarisa.maid_restaurant.cooktask.SteamerCookTask;
import com.mastermarisa.maid_restaurant.cooktask.StockpotCookTask;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class CookTasks {
    private static final ConcurrentHashMap<RecipeType<?>, ICookTask> taskMap;
    private static final ConcurrentHashMap<String, RecipeType<?>> typeMap;
    private static final ConcurrentHashMap<RecipeType<?>, String> typePool;
    private static final List<ICookTask> toRegister;

    public static void register(ICookTask task) {
        toRegister.add(task);
    }

    public static void register() {
        for (var task : toRegister) {
            taskMap.put(task.getType(),task);
            typeMap.put(task.getUID(),task.getType());
            typePool.put(task.getType(),task.getUID());
        }
    }

    public static ICookTask getTask(RecipeType<?> type) {
        return taskMap.get(type);
    }

    public static String getUID(RecipeType<?> type) {
        return typePool.getOrDefault(type,"");
    }

    public static RecipeType<?> getType(String UID) {
        return typeMap.get(UID);
    }

    public static List<ICookTask> getRegistered() { return toRegister.stream().toList(); }

    static {
        taskMap = new ConcurrentHashMap<>();
        typeMap = new ConcurrentHashMap<>();
        typePool = new ConcurrentHashMap<>();
        toRegister = new ArrayList<>();
        register(new StockpotCookTask());
        register(new PotCookTask());
        register(new SteamerCookTask());
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event){
        register();
    }
}
