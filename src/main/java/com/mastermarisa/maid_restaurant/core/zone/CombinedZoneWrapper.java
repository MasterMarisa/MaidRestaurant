package com.mastermarisa.maid_restaurant.core.zone;

import com.google.common.collect.Iterators;
import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CombinedZoneWrapper extends AbstractZone {
    private final List<AbstractZone> zones;

    public CombinedZoneWrapper(AbstractZone... zones) {
        this.zones = Arrays.asList(zones);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return this.zones.stream().anyMatch(z -> z.contains(pos));
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return Iterators.concat(zones.stream().map(AbstractZone::iterator).iterator());
    }
}
