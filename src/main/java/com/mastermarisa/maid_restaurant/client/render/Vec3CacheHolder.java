package com.mastermarisa.maid_restaurant.client.render;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Vec3CacheHolder {
    @Nullable
    private Vec3 cache;

    public Vec3 lerpTo(Vec3 target, double t) {
        cache = cache == null ? target : lerp(cache, target, t);
        return cache;
    }

    public static Vec3 lerp(Vec3 start, Vec3 end, double t) {
        double x = start.x + (end.x - start.x) * t;
        double y = start.y + (end.y - start.y) * t;
        double z = start.z + (end.z - start.z) * t;
        return new Vec3(x, y, z);
    }
}
