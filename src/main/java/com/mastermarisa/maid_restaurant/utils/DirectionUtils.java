package com.mastermarisa.maid_restaurant.utils;

import net.minecraft.core.Direction;

public class DirectionUtils {
    public static Direction getClosestDirection(double x, double y, double z) {
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        double max = Math.max(absX, Math.max(absY, absZ));

        if (max == absX) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        } else if (max == absY) {
            return y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    public static Direction getHorizontalDirection(double x, double z) {
        double angle = Math.atan2(z, x) * (180 / Math.PI);

        if (angle < -135 || angle >= 135) return Direction.WEST;
        if (angle < -45) return Direction.NORTH;
        if (angle < 45) return Direction.EAST;
        return Direction.SOUTH;
    }
}
