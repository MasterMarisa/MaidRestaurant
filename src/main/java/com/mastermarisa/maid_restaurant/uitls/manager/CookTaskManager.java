package com.mastermarisa.maid_restaurant.uitls.manager;

import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.task.cooktask.PotCookTask;
import com.mastermarisa.maid_restaurant.task.cooktask.SteamerCookTask;
import com.mastermarisa.maid_restaurant.task.cooktask.StockpotCookTask;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CookTaskManager {
    private static final ConcurrentHashMap<RecipeType<?>, ICookTask> taskMap;
    private static final ConcurrentHashMap<ICookTask, RecipeType<?>> taskPool;
    private static final ConcurrentHashMap<String, RecipeType<?>> typeMap;
    private static final ConcurrentHashMap<RecipeType<?>, String> typePool;
    private static final List<RecipeType<?>> orderedTypes;
    public static final LinkedList<ICookTask> toRegister;

    public static void register(ICookTask task) {
        toRegister.add(task);
    }

    public static void register() {
        taskMap.clear();
        taskPool.clear();
        typeMap.clear();
        taskPool.clear();
        orderedTypes.clear();
        for (var task : toRegister) {
            taskMap.put(task.getType(),task);
            taskPool.put(task,task.getType());
            typeMap.put(task.getUID(),task.getType());
            typePool.put(task.getType(),task.getUID());
            orderedTypes.add(task.getType());
        }
    }

    public static Optional<ICookTask> getTask(RecipeType<?> type) {
        if (taskMap.containsKey(type)) return Optional.of(taskMap.get(type));
        return Optional.empty();
    }

    public static Optional<ICookTask> getTask(String UID) {
        if (typeMap.containsKey(UID)) return getTask(typeMap.get(UID));
        return Optional.empty();
    }

    public static String getUID(RecipeType<?> type) {
        if (typePool.containsKey(type)) return typePool.get(type);
        return "";
    }

    public static RecipeType<?> getType(String UID) {
        if (typeMap.containsKey(UID)) return typeMap.get(UID);
        return null;
    }

    public static List<RecipeType<?>> getAllRegisteredTypes() {
        return orderedTypes;
    }

    static {
        toRegister = new LinkedList<>();
        taskMap = new ConcurrentHashMap<>();
        taskPool = new ConcurrentHashMap<>();
        typeMap = new ConcurrentHashMap<>();
        typePool = new ConcurrentHashMap<>();
        orderedTypes = new ArrayList<>();
        register(new StockpotCookTask());
        register(new PotCookTask());
        register(new SteamerCookTask());
    }
}
