package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PosUtil {
    public static double distSqrHorizontal(EntityMaid maid, BlockPos pos) {
        double dx = maid.getX() - (pos.getX() + 0.5);
        double dz = maid.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dz * dz;
    }

    protected static boolean isEmptyBlockPos(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(
                level,
                pos,
                CollisionContext.empty()
        ).isEmpty();
    }

    public static boolean isSafePos(Level level, BlockPos pos) {
        return isEmptyBlockPos(level, pos)
                && isEmptyBlockPos(level, pos.above())
                && !isEmptyBlockPos(level, pos.below());
    }

    public static boolean findHorizontal(BlockPos pos, Predicate<BlockPos> predicate, List<BlockPos> results) {
        int preSize = results.size();
        if (predicate.test(pos.north())) results.add(pos.north());
        if (predicate.test(pos.east())) results.add(pos.east());
        if (predicate.test(pos.south())) results.add(pos.south());
        if (predicate.test(pos.west())) results.add(pos.west());
        return results.size() > preSize;
    }

    @Nullable
    public static BlockPos findNearestSafePosHorizontal(Level level, EntityMaid maid, BlockPos pos) {
        List<BlockPos> results = new ArrayList<>();
        if (!findHorizontal(pos, p -> isSafePos(level, p), results)) {
            return null;
        }

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (var blockPos : results) {
            double dist = distSqrHorizontal(maid, blockPos);
            if (dist < bestDist) {
                best = blockPos;
                bestDist = dist;
            }
        }

        return best;
    }

    public static Direction getHorizontalDirection(double x, double z) {
        double angle = Math.atan2(z, x) * (180 / Math.PI);

        if (angle < -135 || angle >= 135) return Direction.WEST;
        if (angle < -45) return Direction.NORTH;
        if (angle < 45) return Direction.EAST;
        return Direction.SOUTH;
    }
}
