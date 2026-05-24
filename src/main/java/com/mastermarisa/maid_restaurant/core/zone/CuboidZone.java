package com.mastermarisa.maid_restaurant.core.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CuboidZone extends AbstractZone {
    private static final String TAG_MIN = "min";
    private static final String TAG_MAX = "max";

    private BlockPos min;
    private BlockPos max;

    public CuboidZone() {
        this.min = BlockPos.ZERO;
        this.max = BlockPos.ZERO;
    }

    public CuboidZone(BlockPos p1, BlockPos p2) {
        this.min = new BlockPos(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
        );
        this.max = new BlockPos(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())
        );
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    @Override
    public boolean contains(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new CuboidIterator(min, max);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_MIN, min.asLong());
        tag.putLong(TAG_MAX, max.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_MIN)) {
            min = BlockPos.of(tag.getLong(TAG_MIN));
        }
        if (tag.contains(TAG_MAX)) {
            max = BlockPos.of(tag.getLong(TAG_MAX));
        }
    }

    public static CuboidZone fromNBT(CompoundTag tag) {
        CuboidZone zone = new CuboidZone();
        zone.deserializeNBT(tag);
        return zone;
    }

    public static class CuboidIterator implements Iterator<BlockPos> {
        private final BlockPos min;
        private final BlockPos max;
        private final int total;
        private int index;

        public CuboidIterator(BlockPos min, BlockPos max) {
            this.min = min;
            this.max = max;
            this.total = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < total;
        }

        @Override
        public BlockPos next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int rem = index;
            int dz = max.getZ() - min.getZ() + 1;
            int dy = max.getY() - min.getY() + 1;
            int z = min.getZ() + (rem % dz);
            rem /= dz;
            int y = min.getY() + (rem % dy);
            rem /= dy;
            int x = min.getX() + rem;
            index++;
            return new BlockPos(x, y, z);
        }
    }
}
