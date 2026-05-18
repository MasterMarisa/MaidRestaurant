package com.mastermarisa.maid_restaurant.core.capability;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CapabilityRegistry {
    private static final Map<String, ICookCapability> registry;

    public static void register(ICookCapability capability) {
        registry.put(capability.getUID(), capability);
    }

    @Nullable
    public static ICookCapability get(String uid) {
        return registry.getOrDefault(uid, null);
    }

    public static Collection<ICookCapability> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static void clear() {
        registry.clear();
    }

    static {
        registry = new LinkedHashMap<>();
        register(new CraftingTableCapability());
    }
}
