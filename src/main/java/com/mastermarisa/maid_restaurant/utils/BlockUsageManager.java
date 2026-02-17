package com.mastermarisa.maid_restaurant.utils;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.util.PooledStringHashSet;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.util.StringPool;
import net.minecraft.core.BlockPos;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockUsageManager {
    private static final ConcurrentHashMap<Long, PooledStringHashSet> blockUsage = new ConcurrentHashMap<>();

    public static void reset() { blockUsage.clear(); }

    public static void addUser(BlockPos pos, UUID uuid){
        blockUsage.computeIfAbsent(encode(pos),(k)-> new PooledStringHashSet(2)).add(uuid.toString());
    }

    public static void removeUser(BlockPos pos, UUID uuid){
        if (blockUsage.containsKey(encode(pos))){
            blockUsage.get(encode(pos)).remove(StringPool.computeIfAbsent(uuid.toString()));
        }
    }

    public static boolean isUsing(BlockPos pos, UUID uuid){
        return blockUsage.computeIfAbsent(encode(pos),(k)-> new PooledStringHashSet(2)).contains(uuid.toString());
    }

    public static int getUserCount(BlockPos pos){
        return blockUsage.computeIfAbsent(encode(pos),(k)-> new PooledStringHashSet(2)).size();
    }

    private static Long encode(BlockPos pos){
        return pos.asLong();
    }
}
