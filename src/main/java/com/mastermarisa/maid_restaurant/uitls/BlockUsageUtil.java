package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.util.PooledStringHashSet;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.util.StringPool;
import net.minecraft.core.BlockPos;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockUsageUtil {
    private static final ConcurrentHashMap<Long, PooledStringHashSet> map;

    public static void add(BlockPos pos, UUID uuid){
        map.computeIfAbsent(pos.asLong(), l -> new PooledStringHashSet(2)).add(uuid.toString());
    }

    public static void remove(BlockPos pos, UUID uuid){
        if (map.containsKey(pos.asLong())) {
            map.get(pos.asLong()).remove(StringPool.computeIfAbsent(uuid.toString()));
        }
    }

    public static boolean isUsing(BlockPos pos, UUID uuid){
        return map.computeIfAbsent(pos.asLong(), l -> new PooledStringHashSet(2)).contains(uuid.toString());
    }

    public static boolean isUsed(BlockPos pos) {
        if (map.containsKey(pos.asLong())) {
            return !map.get(pos.asLong()).isEmpty();
        }
        return false;
    }

    static {
        map = new ConcurrentHashMap<>();
    }
}
