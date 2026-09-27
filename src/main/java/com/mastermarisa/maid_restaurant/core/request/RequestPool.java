package com.mastermarisa.maid_restaurant.core.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class RequestPool<T extends INBTSerializable<CompoundTag>> {
    private final Function<CompoundTag, T> deserializer;
    private final List<Entry> entries = new ArrayList<>();

    public RequestPool(Function<CompoundTag, T> deserializer) {
        this.deserializer = deserializer;
    }

    public void enqueue(T request) {
        entries.add(new Entry(request));
    }

    @Nullable
    public T claim(EntityMaid maid) {
        UUID id = maid.getUUID();
        for (Entry e : entries) {
            if (id.equals(e.owner)) return e.request;
        }
        for (Entry e : entries) {
            if (e.owner == null) {
                e.owner = id;
                return e.request;
            }
        }
        return null;
    }

    @Nullable
    public T getClaimed(EntityMaid maid) {
        UUID id = maid.getUUID();
        for (Entry e : entries) {
            if (id.equals(e.owner)) return e.request;
        }
        return null;
    }

    public boolean release(EntityMaid maid) {
        UUID id = maid.getUUID();
        for (Entry e : entries) {
            if (id.equals(e.owner)) {
                e.owner = null;
                return true;
            }
        }
        return false;
    }

    @Nullable
    public T submit(EntityMaid maid) {
        UUID id = maid.getUUID();
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (id.equals(e.owner)) {
                it.remove();
                return e.request;
            }
        }
        return null;
    }

    public void clear() {
        entries.clear();
    }

    public boolean cleanup(BiPredicate<T, UUID> isOwnerValid, Predicate<T> isRequestValid) {
        boolean changed = false;
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();

            if (!isRequestValid.test(e.request)) {
                it.remove();
                changed = true;
                continue;
            }

            if (e.owner == null) continue;
            if (!isOwnerValid.test(e.request, e.owner)) {
                e.owner = null;
                changed = true;
            }
        }
        return changed;
    }

    public boolean isEmpty() { return entries.isEmpty(); }

    public ListTag save() {
        ListTag list = new ListTag();
        for (Entry e : entries) {
            list.add(e.serializeNBT());
        }
        return list;
    }

    public void load(ListTag list) {
        entries.clear();
        for (int i = 0; i < list.size(); i++) {
            Entry e = new Entry();
            e.deserializeNBT(list.getCompound(i));
            entries.add(e);
        }
    }

    private class Entry implements INBTSerializable<CompoundTag> {
        private T request;
        @Nullable
        private UUID owner;

        Entry() {}
        Entry(T request) { this.request = request; }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("request", request.serializeNBT());
            if (owner != null) {
                tag.putUUID("owner", owner);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            this.request = deserializer.apply(tag.getCompound("request"));
            if (tag.contains("owner")) {
                this.owner = tag.getUUID("owner");
            }
        }
    }
}
