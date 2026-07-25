package com.mastermarisa.maid_restaurant.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StorageRegistry {
    private static final Map<ResourceLocation, IMaidStorage> registry = new LinkedHashMap<>();
    private static final List<IMaidStorage> ordered = new ArrayList<>();

    public static void register(IMaidStorage storage) {
        registry.put(storage.getID(), storage);
        ordered.add(storage);
        ordered.sort(Comparator.comparingInt(IMaidStorage::getPriority));
    }

    @Nullable
    public static IMaidStorage get(ResourceLocation id) {
        return registry.getOrDefault(id, null);
    }

    public static Collection<IMaidStorage> getAll() {
        return Collections.unmodifiableCollection(ordered);
    }

    public static void clear() {
        registry.clear();
    }

    @Nullable
    public static IMaidStorage tryGetAt(Level level, BlockPos pos) {
        for (var storage : getAll()) {
            if (storage.isValid(level, pos)) {
                return storage;
            }
        }
        return null;
    }
}
