package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.EncodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServeRequestQueue implements INBTSerializable<CompoundTag> {
    private LinkedList<ServeRequest> requests = new LinkedList<>();

    public void add(ServeRequest request) {
        requests.add(request);
    }

    public void remove(ServeRequest request) { requests.remove(request); }

    public Optional<ServeRequest> peek() {
        if (!requests.isEmpty()) return Optional.of(requests.getFirst());
        return Optional.empty();
    }

    public Optional<ServeRequest> pop() {
        if (!requests.isEmpty()) return Optional.of(requests.removeFirst());
        return Optional.empty();
    }

    public void removeAt(int index) {
        if (index < requests.size())
            requests.remove(index);
    }

    public int size() { return requests.size(); }

    public Iterator<ServeRequest> iterator() { return requests.iterator(); }

    public List<ServeRequest> toList() { return new ArrayList<>(requests); }

    public void clear() { requests.clear(); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag targetTables = new ListTag();
        ListTag foodIDs = new ListTag();
        ListTag providers = new ListTag();
        List<Integer> counts = new ArrayList<>();
        List<Integer> requestedCounts = new ArrayList<>();
        for (ServeRequest request : requests) {
            targetTables.add(new LongArrayTag(request.targetTables.stream().map(BlockPos::asLong).toList()));
            foodIDs.add(StringTag.valueOf(EncodeUtils.encode(request.toServe).toString()));
            providers.add(StringTag.valueOf(request.provider.toString()));
            counts.add(request.toServe.getCount());
            requestedCounts.add(request.requestedCount);
        }
        tag.put("targetTables",targetTables);
        tag.put("foodIDs",foodIDs);
        tag.put("providers",providers);
        tag.putIntArray("counts",counts);
        tag.putIntArray("requestedCounts",requestedCounts);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("targetTables")) {
            requests = new LinkedList<>();
            ListTag targetTables = tag.getList("targetTables", Tag.TAG_LONG_ARRAY);
            ListTag foodIDs = tag.getList("foodIDs",Tag.TAG_STRING);
            ListTag providers = tag.getList("providers",Tag.TAG_STRING);
            int[] counts = tag.getIntArray("counts");
            int[] requestedCounts = tag.getIntArray("requestedCounts");
            for (int i = 0;i < targetTables.size();i++) {
                requests.add(new ServeRequest(new ItemStack(EncodeUtils.decode(foodIDs.getString(i)),counts[i]),targetTables.getLongArray(i), UUID.fromString(providers.getString(i)),requestedCounts[i]));
            }
        }
    }

    public static final AttachmentType<ServeRequestQueue> TYPE = AttachmentType.serializable(ServeRequestQueue::new).sync(new ServeRequestQueueSyncHandler()).build();
}
