package com.mastermarisa.maid_restaurant.core.capability;

import com.mastermarisa.maid_restaurant.api.ICookCapability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CapabilityRegistry {
    private static final Map<String, ICookCapability> registry;

    public static void register(ICookCapability capability) {
        registry.put(capability.getUID(), capability);
    }

    public static ICookCapability get(String uid) {
        return registry.get(uid);
    }

    public static Collection<ICookCapability> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static void clear() {
        registry.clear();
    }

    static {
        registry = new LinkedHashMap<>();
    }
}
