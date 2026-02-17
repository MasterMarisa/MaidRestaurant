package com.mastermarisa.maid_restaurant.utils;

import net.minecraft.util.RandomSource;

import java.util.concurrent.ConcurrentHashMap;

public class CheckRateManager {
    private static final ConcurrentHashMap<String,Integer> nextCheckRateMap;

    public static boolean check(String key, int maxCheckRate, RandomSource source) {
        nextCheckRateMap.putIfAbsent(key, 0);
        if (nextCheckRateMap.get(key) > 0){
            nextCheckRateMap.put(key, nextCheckRateMap.get(key)-1);
            return false;
        } else {
            nextCheckRateMap.put(key,maxCheckRate + source.nextInt(maxCheckRate));
            return true;
        }
    }

    public static void setNextCheckTick(String key, int tick) {
        nextCheckRateMap.put(key,tick);
    }

    public static void remove(String key) {
        nextCheckRateMap.remove(key);
    }

    public static void reset() {
        nextCheckRateMap.clear();
    }

    static {
        nextCheckRateMap = new ConcurrentHashMap<>();
    }
}
