package com.mastermarisa.maid_restaurant.core.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StorageRegistry {
    private static final Map<String, IMaidStorage> registry;

    public static void register(IMaidStorage storage) {
        registry.put(storage.getUID(), storage);
    }

    public static IMaidStorage get(String uid) {
        return registry.get(uid);
    }

    public static Collection<IMaidStorage> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static void clear() {
        registry.clear();
    }

    @Nullable
    public static IMaidStorage tryGet(Level level, BlockPos pos) {
        for (var storage : getAll()) {
            if (storage.isValid(level, pos)) {
                return storage;
            }
        }
        return null;
    }

    static {
        registry = new LinkedHashMap<>();
        register(new CommonStorage());
    }
}
