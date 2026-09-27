package com.mastermarisa.maid_restaurant.core.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class RequestBus<T extends INBTSerializable<CompoundTag>> extends SavedData {
    protected final Map<String, RequestPool<T>> pools = new ConcurrentHashMap<>();
    private final Function<CompoundTag, T> deserializer;

    protected RequestBus(Function<CompoundTag, T> deserializer) {
        this.deserializer = deserializer;
    }

    @Nullable
    public RequestPool<T> pool(String restaurantId) {
        return pools.get(restaurantId);
    }

    public void enqueue(String restaurantId, T request) {
        pools.computeIfAbsent(restaurantId, k -> new RequestPool<>(deserializer)).enqueue(request);
        setDirty();
    }

    @Nullable
    public T claim(String restaurantId, EntityMaid maid) {
        RequestPool<T> pool = pool(restaurantId);
        if (pool == null) return null;
        T result = pool.claim(maid);
        if (result != null) setDirty();
        return result;
    }

    @Nullable
    public T getClaimed(String restaurantId, EntityMaid maid) {
        RequestPool<T> pool = pool(restaurantId);
        if (pool == null) return null;
        return pool.getClaimed(maid);
    }

    public boolean release(String restaurantId, EntityMaid maid) {
        RequestPool<T> pool = pool(restaurantId);
        if (pool == null) return false;
        return pool.release(maid);
    }

    @Nullable
    public T submit(String restaurantId, EntityMaid maid) {
        RequestPool<T> pool = pool(restaurantId);
        if (pool == null) return null;
        T result = pool.submit(maid);
        if (result != null) setDirty();
        return result;
    }

    public void clear(String restaurantId) {
        RequestPool<T> pool = pool(restaurantId);
        if (pool == null) return;
        pool.clear();
        setDirty();
    }

    protected abstract boolean isOwnerValid(ServerLevel level, String restaurantId,
                                            T request, UUID owner);

    protected abstract boolean isRequestValid(ServerLevel level, String restaurantId, T request);

    public void cleanup(ServerLevel level) {
        boolean changed = false;
        for (var e : pools.entrySet()) {
            String restaurantId = e.getKey();
            RequestPool<T> pool = e.getValue();
            boolean poolChanged = pool.cleanup(
                    (t, u) -> isOwnerValid(level, restaurantId, t, u),
                    t -> isRequestValid(level, restaurantId, t)
            );
            if (poolChanged) changed = true;
        }
        if (changed) setDirty();
    }

    public abstract void unload(ServerLevel level);

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag poolTag = new ListTag();
        for (var e : pools.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("restaurant_id", e.getKey());
            entryTag.put("entry_list", e.getValue().save());
            poolTag.add(entryTag);
        }
        tag.put("pool", poolTag);
        return tag;
    }

    protected void load(CompoundTag tag) {
        if (!tag.contains("pool")) return;
        ListTag poolTag = tag.getList("pool", Tag.TAG_COMPOUND);
        for (int i = 0; i < poolTag.size(); i++) {
            CompoundTag entryTag = poolTag.getCompound(i);
            String restaurantId = entryTag.getString("restaurant_id");
            RequestPool<T> pool = new RequestPool<>(deserializer);
            pool.load(entryTag.getList("entry_list", Tag.TAG_COMPOUND));
            pools.put(restaurantId, pool);
        }
    }
}
