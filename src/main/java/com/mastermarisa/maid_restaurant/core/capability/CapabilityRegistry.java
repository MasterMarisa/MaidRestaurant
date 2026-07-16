package com.mastermarisa.maid_restaurant.core.capability;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class CapabilityRegistry {
    private static final Map<String, ICookCapability> registry = new LinkedHashMap<>();
    private static final Map<RecipeType<?>, ICookCapability> typeMap = new LinkedHashMap<>();

    public static void register(ICookCapability capability) {
        registry.put(capability.getUID(), capability);
        typeMap.put(capability.getRecipeType(), capability);
    }

    @Nullable
    public static ICookCapability get(String uid) {
        return registry.getOrDefault(uid, null);
    }

    @Nullable
    public static ICookCapability get(RecipeType<?> type) {
        return typeMap.getOrDefault(type, null);
    }

    public static List<RecipeType<?>> getRegisteredTypes() {
        return typeMap.keySet().stream().toList();
    }

    public static Collection<ICookCapability> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static int size() {
        return registry.size();
    }

    public static boolean contains(String uid) {
        return registry.containsKey(uid);
    }

    public static boolean contains(RecipeType<?> type) {
        return typeMap.containsKey(type);
    }

    public static void clear() {
        registry.clear();
    }
}
