package com.mastermarisa.maid_restaurant.data.zone;

import com.google.common.collect.Iterators;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class CombinedZoneWrapper extends AbstractZone {
    private final List<AbstractZone> zones;

    public CombinedZoneWrapper(List<AbstractZone> zones) {
        this.zones = new ArrayList<>(zones);
    }

    public CombinedZoneWrapper(AbstractZone... zones) {
        this.zones = new ArrayList<>(Arrays.asList(zones));
    }

    @Override
    public boolean contains(BlockPos pos) {
        return this.zones.stream().anyMatch(z -> z.contains(pos));
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return Iterators.concat(zones.stream().map(AbstractZone::iterator).iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CombinedZoneWrapper other)) return false;
        return zones.equals(other.zones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zones);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (AbstractZone zone : zones) {
            listTag.add(AbstractZone.REGISTRY.serialize(zone));
        }
        tag.put("zones", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        zones.clear();
        ListTag listTag = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            zones.add(AbstractZone.REGISTRY.deserialize(listTag.getCompound(i)));
        }
    }

    public static CombinedZoneWrapper fromNBT(CompoundTag tag) {
        CombinedZoneWrapper wrapper = new CombinedZoneWrapper();
        wrapper.deserializeNBT(tag);
        return wrapper;
    }
}
