package com.mastermarisa.maid_restaurant.core.zone;

import com.google.common.collect.Iterators;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (AbstractZone zone : zones) {
            CompoundTag zoneTag = new CompoundTag();
            if (zone instanceof CuboidZone) {
                zoneTag.putString("type", "cuboid");
            } else if (zone instanceof PointsetZone) {
                zoneTag.putString("type", "pointset");
            } else {
                throw new IllegalArgumentException("Unsupported zone type: " + zone.getClass());
            }
            zoneTag.put("data", zone.serializeNBT());
            listTag.add(zoneTag);
        }
        tag.put("zones", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        zones.clear();
        ListTag listTag = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag zoneTag = listTag.getCompound(i);
            String type = zoneTag.getString("type");
            CompoundTag dataTag = zoneTag.getCompound("data");
            AbstractZone zone = switch (type) {
                case "cuboid" -> CuboidZone.fromNBT(dataTag);
                case "pointset" -> PointsetZone.fromNBT(dataTag);
                default -> throw new IllegalStateException("Unsupported zone type: " + type);
            };
            zones.add(zone);
        }
    }

    public static CombinedZoneWrapper fromNBT(CompoundTag tag) {
        CombinedZoneWrapper wrapper = new CombinedZoneWrapper();
        wrapper.deserializeNBT(tag);
        return wrapper;
    }
}
