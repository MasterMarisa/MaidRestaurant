package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.uitls.SerializerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RequestBus<T extends INBTSerializable<CompoundTag>> extends SavedData {
    private static final ResourceLocation COOKING_REQUEST = MaidRestaurant.modLoc("cooking_request");
    private static final ResourceLocation SERVE_REQUEST = MaidRestaurant.modLoc("serve_request");
    private static final SerializerRegistry<INBTSerializable<CompoundTag>> REGISTRY = new SerializerRegistry<>();

    protected final Map<String, List<RequestEntry<T>>> requestPool = new ConcurrentHashMap<>();

    /**
     * 向请求池中添加一个新的委托
     * @param restaurantId 餐厅Id
     * @param request 要添加的烹饪请求
     */
    public void enqueue(String restaurantId, T request) {
        getEntries(restaurantId).add(new RequestEntry<>(request));
        setDirty();
    }

    /**
     * 认领一个未被占用的委托
     * @param restaurantId 餐厅Id
     * @param maid 女仆实体
     * @return 新认领的委托，如果没有可用委托则返回 null
     */
    @Nullable
    public T claim(String restaurantId, EntityMaid maid) {
        List<RequestEntry<T>> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index == -1) {
            for (var entry : entries) {
                if (entry.owner == null) {
                    setDirty();
                    entry.claim(maid);
                    MaidRestaurant.LOGGER.debug("[MaidRestaurant-DEBUG] Request Claimed.");
                    return entry.request;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前女仆已认领的委托
     * @param restaurantId 餐厅Id
     * @param maid 女仆实体
     * @return 已认领的委托
     */
    @Nullable
    public T getClaimed(String restaurantId, EntityMaid maid) {
        List<RequestEntry<T>> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        return index != -1 ? entries.get(index).request : null;
    }

    /**
     * 释放当前女仆占用的委托
     * @param restaurantId 餐厅Id
     * @param maid 女仆实体
     * @return 如果成功释放则返回 true，否则返回 false
     */
    public boolean release(String restaurantId, EntityMaid maid) {
        List<RequestEntry<T>> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index != -1) {
            setDirty();
            entries.get(index).release();
            return true;
        }
        return false;
    }

    /**
     * 提交已完成委托，从请求池中移除当前女仆占用的委托
     * @param restaurantId 餐厅Id
     * @param maid 女仆实体
     * @return 如果成功移除则返回 true，否则返回 false
     */
    @Nullable
    public T submit(String restaurantId, EntityMaid maid) {
        List<RequestEntry<T>> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index != -1) {
            setDirty();
            return entries.remove(index).request;
        }
        return null;
    }

    public void clear(String restaurantId) {
        getEntries(restaurantId).clear();
        setDirty();
    }

    protected int findEntryIndex(List<RequestEntry<T>> entries, EntityMaid maid) {
        for (int i = 0; i < entries.size(); i++) {
            RequestEntry<T> entry = entries.get(i);
            if (entry.owner != null && entry.owner.equals(maid.getUUID())) {
                return i;
            }
        }
        return -1;
    }

    protected List<RequestEntry<T>> getEntries(String restaurantId) {
        return requestPool.computeIfAbsent(restaurantId, k -> new ArrayList<>());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag poolTag = new ListTag();
        for (var entry : requestPool.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("restaurant_id", entry.getKey());
            ListTag listTag = new ListTag();
            for (var request : entry.getValue()) {
                listTag.add(request.serializeNBT());
            }
            entryTag.put("entry_list", listTag);
            poolTag.add(entryTag);
        }
        tag.put("pool", poolTag);
        return tag;
    }

    protected void load(CompoundTag tag) {
        if (tag.contains("pool")) {
            ListTag poolTag = tag.getList("pool", Tag.TAG_COMPOUND);
            for (int i = 0; i < poolTag.size(); i++) {
                CompoundTag entryTag = poolTag.getCompound(i);
                String restaurantId = entryTag.getString("restaurant_id");
                List<RequestEntry<T>> entryList = new LinkedList<>();
                for (var request : entryTag.getList("entry_list", Tag.TAG_COMPOUND)) {
                    entryList.add(RequestEntry.fromNBT((CompoundTag) request));
                }
                requestPool.put(restaurantId, entryList);
            }
        }
    }

    public static void registerSerializers() {
        REGISTRY.register(COOKING_REQUEST, CookingRequest.class, CookingRequest::fromNBT);
        REGISTRY.register(SERVE_REQUEST, ServeRequest.class, ServeRequest::fromNBT);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends INBTSerializable<CompoundTag>> T deserializeRequest(CompoundTag tag) {
        return (T) REGISTRY.deserialize(tag);
    }

    protected static class RequestEntry<T extends INBTSerializable<CompoundTag>> implements INBTSerializable<CompoundTag> {
        protected T request;
        @Nullable
        protected UUID owner;

        public RequestEntry() {}

        public RequestEntry(T request) {
            this.request = request;
        }

        public void claim(EntityMaid maid) {
            this.owner = maid.getUUID();
        }

        public void release() {
            this.owner = null;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("request", REGISTRY.serialize(request));
            if (owner != null) {
                tag.putUUID("owner", owner);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("request")) {
                this.request = deserializeRequest(tag.getCompound("request"));
            }
            if (tag.contains("owner")) {
                this.owner = tag.getUUID("owner");
            }
        }

        public static <T extends INBTSerializable<CompoundTag>> RequestEntry<T> fromNBT(CompoundTag tag) {
            RequestEntry<T> entry = new RequestEntry<>();
            entry.deserializeNBT(tag);
            return entry;
        }
    }
}
