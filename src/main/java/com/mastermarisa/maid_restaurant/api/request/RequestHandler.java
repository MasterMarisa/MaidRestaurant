package com.mastermarisa.maid_restaurant.api.request;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public abstract class RequestHandler <T extends IRequest> implements INBTSerializable<CompoundTag> {
    protected LinkedList<T> requests = new LinkedList<>();
    public boolean accept = true;

    public LinkedList<T> getRequests() { return requests; }

    public int size() { return requests.size(); }

    public void add(T request) {
        requests.add(request);
    }

    public @Nullable T getFirst() {
        if (requests.isEmpty()) return null;
        return requests.getFirst();
    }

    public @Nullable T removeFirst() {
        if (requests.isEmpty()) return null;
        return requests.removeFirst();
    }

    public @Nullable T getAt(int index) {
        if (requests.size() <= index) return null;
        return requests.get(index);
    }

    public @Nullable T removeAt(int index) {
        if (requests.size() <= index) return null;
        return requests.remove(index);
    }

    public boolean remove(T value) {
        return requests.remove(value);
    }

    public void clear() { requests.clear(); }

    public List<T> toList() { return requests.stream().toList(); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("length",requests.size());
        for (int i = 0;i < requests.size();i++) tag.put(String.valueOf(i),requests.get(i).serializeNBT(provider));
        tag.putBoolean("accept",accept);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (!tag.contains("length")) return;
        requests = new LinkedList<>();
        for (int i = 0;i < tag.getInt("length");i++) {
            requests.add(fromCompound(provider,tag.getCompound(String.valueOf(i))));
        }
        accept = tag.getBoolean("accept");
    }

    protected abstract T fromCompound(HolderLookup.Provider provider, CompoundTag tag);
}
