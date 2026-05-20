package com.mastermarisa.maid_restaurant.core.capability;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CapabilityRegistry {
    private static final Map<String, ICookCapability> registry = new LinkedHashMap<>();
    private static final Map<RecipeType<?>, ICookCapability> typeMap = new ConcurrentHashMap<>();

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

    public static Collection<ICookCapability> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static void clear() {
        registry.clear();
    }

    static {
        register(new CraftingTableCapability());
    }
}
