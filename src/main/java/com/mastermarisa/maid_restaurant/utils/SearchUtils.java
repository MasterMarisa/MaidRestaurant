package com.mastermarisa.maid_restaurant.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchUtils {
    public static List<BlockPos> search(BlockPos center, int range, int verticalRange, Predicate<BlockPos> filter) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.withinManhattan(center,range,verticalRange,range)) {
            if (filter.test(pos)) {
                found.add(pos.immutable());
            }
        }

        return found;
    }

    public static @Nullable BlockPos ground(ServerLevel level, BlockPos center, int range) {
        BlockPos pos = center.immutable().below();
        BlockState state = level.getBlockState(pos);
        while (range > 0 && !state.isCollisionShapeFullBlock(level,pos)) {
            pos = pos.below();
            state = level.getBlockState(pos);
            range--;
        }

        return state.isCollisionShapeFullBlock(level,pos) ? pos.immutable().above() : null;
    }

    public static @Nullable BlockPos getRelativeGround(ServerLevel level, BlockPos center, int range, Direction dir) {
        return ground(level,center.immutable().relative(dir),range);
    }

    public static List<BlockPos> getAllRelativeGround(ServerLevel level, BlockPos center, int range) {
        List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        return dirs.stream().map(d->getRelativeGround(level,center,range,d)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
