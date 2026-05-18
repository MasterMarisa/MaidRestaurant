package com.mastermarisa.maid_restaurant.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.util.RandomSource;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckRateHelper {
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Integer>> map;

    public static boolean shouldCheck(UUID uuid, String key, RandomSource random, int maxInterval) {
        ConcurrentHashMap<String, Integer> subMap = map.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
        subMap.putIfAbsent(key, 0);
        if (subMap.get(key) > 0) {
            subMap.put(key, subMap.get(key) - 1);
            return false;
        } else {
            int halfInterval = maxInterval / 2;
            int offset = maxInterval % 2 == 0 ? 1 : 2;
            subMap.put(key, halfInterval + random.nextInt(halfInterval + offset));
            return true;
        }
    }

    public static boolean shouldCheck(EntityMaid maid, String key, int maxInterval) {
        return shouldCheck(maid.getUUID(), key, maid.getRandom(), maxInterval);
    }

    public static void setRemainingTicks(UUID uuid, String key, int tick) {
        ConcurrentHashMap<String, Integer> subMap = map.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
        subMap.put(key, tick);
    }

    static {
        map = new ConcurrentHashMap<>();
    }
}
