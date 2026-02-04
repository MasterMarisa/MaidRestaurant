package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class BlockPosUtils {
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
        return dirs.stream().map(d->getRelativeGround(level,center,range,d)).filter(Objects::nonNull).toList();
    }

    public static List<BlockPos> unpack(long[] packed) {
        List<BlockPos> ans = new ArrayList<>();
        for (long l : packed) {
            ans.add(new BlockPos(BlockPos.getX(l),BlockPos.getY(l),BlockPos.getZ(l)));
        }
        return ans;
    }

    public static BlockPos unpack(long packed) {
        return new BlockPos(BlockPos.getX(packed),BlockPos.getY(packed),BlockPos.getZ(packed));
    }

    public static long[] pack(List<BlockPos> pos) {
        long[] packed = new long[pos.size()];
        for (int i = 0;i < pos.size();i++){
            packed[i] = pos.get(i).asLong();
        }

        return packed;
    }
}
