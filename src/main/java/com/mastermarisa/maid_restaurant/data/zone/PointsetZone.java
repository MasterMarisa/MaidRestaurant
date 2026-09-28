package com.mastermarisa.maid_restaurant.data.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class PointsetZone extends AbstractZone {
    private static final String TAG_POINTS = "points";

    private final Set<BlockPos> points;

    public PointsetZone() {
        this.points = new ConcurrentSkipListSet<>();
    }

    public void add(BlockPos pos) {
        points.add(pos);
    }

    public void remove(BlockPos pos) {
        points.remove(pos);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return points.stream().anyMatch(pos::equals);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return points.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointsetZone other)) return false;
        return points.equals(other.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray(TAG_POINTS, points.stream().map(BlockPos::asLong).toList());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_POINTS)) {
            points.clear();
            points.addAll(Arrays.stream(tag.getLongArray(TAG_POINTS)).boxed().map(BlockPos::of).toList());
        }
    }

    public static PointsetZone fromNBT(CompoundTag tag) {
        PointsetZone zone = new PointsetZone();
        zone.deserializeNBT(tag);
        return zone;
    }
}
