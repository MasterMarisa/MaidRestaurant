package com.mastermarisa.maid_restaurant.core.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PointSetZone extends AbstractZone implements INBTSerializable<CompoundTag> {
    private static final String TAG_POINTS = "points";

    private final List<BlockPos> points;

    public PointSetZone() {
        this.points = new LinkedList<>();
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

    public static PointSetZone fromNBT(CompoundTag tag) {
        PointSetZone zone = new PointSetZone();
        zone.deserializeNBT(tag);
        return zone;
    }
}
