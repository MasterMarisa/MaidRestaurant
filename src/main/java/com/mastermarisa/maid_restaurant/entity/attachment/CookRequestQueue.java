package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class CookRequestQueue implements INBTSerializable<CompoundTag> {
    private LinkedList<CookRequest> requests = new LinkedList<>();

    public void add(CookRequest request) {
        requests.add(request);
    }

    public void remove(CookRequest request) { requests.remove(request); }

    public Optional<CookRequest> peek() {
        if (!requests.isEmpty()) return Optional.of(requests.getFirst());
        return Optional.empty();
    }

    public Optional<CookRequest> pop() {
        if (!requests.isEmpty()) return Optional.of(requests.removeFirst());
        return Optional.empty();
    }

    public void removeAt(int index) {
        if (index < requests.size())
            requests.remove(index);
    }

    public int size() { return requests.size(); }

    public Iterator<CookRequest> iterator() { return requests.iterator(); }

    public List<CookRequest> toList() { return new ArrayList<>(requests); }

    public void clear() { requests.clear(); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag listID = new ListTag();
        ListTag listType = new ListTag();
        ListTag listCount = new ListTag();
        ListTag listMaxCount = new ListTag();
        for (CookRequest request : requests) {
            listID.add(StringTag.valueOf(request.id.toString()));
            listType.add(StringTag.valueOf(CookTaskManager.getUID(request.type)));
            listCount.add(IntTag.valueOf(request.count));
            listMaxCount.add(IntTag.valueOf(request.requestedCount));
        }
        tag.put("ID",listID);
        tag.put("Type",listType);
        tag.put("Count",listCount);
        tag.put("requestedCount",listMaxCount);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("ID", Tag.TAG_LIST)) {
            requests = new LinkedList<>();
            ListTag listID = tag.getList("ID", Tag.TAG_STRING);
            ListTag listType = tag.getList("Type", Tag.TAG_STRING);
            ListTag listCount = tag.getList("Count",Tag.TAG_INT);
            ListTag listMaxCount = tag.getList("requestedCount",Tag.TAG_INT);
            for (int i = 0;i < listID.size();i++){
                requests.add(new CookRequest(listID.getString(i),listType.getString(i),listCount.getInt(i),listMaxCount.getInt(i)));
            }
        }
    }

    public static final AttachmentType<CookRequestQueue> TYPE = AttachmentType.serializable(CookRequestQueue::new).sync(new CookRequestQueueSyncHandler()).build();
}
