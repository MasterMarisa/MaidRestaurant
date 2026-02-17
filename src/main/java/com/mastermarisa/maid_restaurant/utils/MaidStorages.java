package com.mastermarisa.maid_restaurant.utils;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.CommonStorage;
import com.mastermarisa.maid_restaurant.storage.FruitBasketStorage;
import com.mastermarisa.maid_restaurant.storage.TableStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MaidStorages {
    private static final ConcurrentHashMap<String, IMaidStorage> typeMap;
    private static final ConcurrentHashMap<IMaidStorage, String> typePool;
    private static final List<IMaidStorage> ordered;

    public static void register(IMaidStorage type) {
        typeMap.put(type.getUID(),type);
        typePool.put(type,type.getUID());
        ordered.add(type);
    }

    public static IMaidStorage getType(String UID) {
        return typeMap.get(UID);
    }

    public static String getUID(IMaidStorage type) {
        return typePool.get(type);
    }

    public static @Nullable IItemHandler tryGetHandler(Level level, BlockPos pos) {
        for (IMaidStorage type : ordered)
            if (type.isValid(level,pos))
                return type.getHandler(level,pos);

        return null;
    }

    public static @Nullable IMaidStorage tryGetType(Level level, BlockPos pos) {
        for (IMaidStorage type : ordered)
            if (type.isValid(level,pos)) return type;

        return null;
    }

    static {
        typeMap = new ConcurrentHashMap<>();
        typePool = new ConcurrentHashMap<>();
        ordered = new ArrayList<>();
        register(new CommonStorage());
        register(new TableStorage());
        register(new FruitBasketStorage());
    }
}
