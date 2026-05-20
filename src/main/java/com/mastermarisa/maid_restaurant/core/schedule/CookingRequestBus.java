package com.mastermarisa.maid_restaurant.core.schedule;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CookingRequestBus extends SavedData {
    private static final String TAG_POOLS = "pools";

    private ConcurrentHashMap<String, RequestPool> pools;

    public CookingRequestBus() {
        pools = new ConcurrentHashMap<>();
    }

    /**
     * 入队一个请求
     * @param restaurantId 餐厅Id
     * @param demand 将入队的请求
     */
    public void submit(String restaurantId, CookingRequest demand) {
        getPool(restaurantId).submit(demand);
    }

    /**
     * 从请求池认领一个请求,若有已认领的请求则不做处理
     * @param restaurantId 餐厅Id
     * @param maidUUID 女仆的UUID
     * @param gameTime 时间戳
     * @return 新认领的请求
     */
    @Nullable
    public CookingRequest claim(String restaurantId, UUID maidUUID, long gameTime) {
        return getPool(restaurantId).claim(maidUUID, gameTime);
    }

    /**
     * 释放当前认领的请求,并尝试从请求池重新认领另一个请求
     * @param restaurantId 餐厅Id
     * @param maidUUID 女仆UUID
     * @param gameTime 时间戳
     * @return 新认领的请求
     */
    @Nullable
    public CookingRequest reclaim(String restaurantId, UUID maidUUID, long gameTime) {
        return getPool(restaurantId).reclaim(maidUUID, gameTime);
    }

    /**
     * 释放女仆已认领的请求
     * @param restaurantId 餐厅Id
     * @param maidUUID 女仆UUID
     */
    public void release(String restaurantId, UUID maidUUID) {
        getPool(restaurantId).release(maidUUID);
    }

    /**
     * 出队一个已完成的请求
     * @param restaurantId 餐厅Id
     * @param maidUUID 女仆UUID
     */
    public void complete(String restaurantId, UUID maidUUID) {
        getPool(restaurantId).complete(maidUUID);
    }

    /**
     * 获取女仆已认领的请求
     * @param restaurantId 餐厅Id
     * @param maidUUID 女仆UUID
     * @return 返还已认领的请求
     */
    @Nullable
    public CookingRequest getClaimed(String restaurantId, UUID maidUUID) {
        return getPool(restaurantId).getClaimed(maidUUID);
    }

    private RequestPool getPool(String restaurantId) {
        return pools.computeIfAbsent(restaurantId, RequestPool::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (var pool : pools.values()) {
            listTag.add(pool.serializeNBT());
        }
        tag.put(TAG_POOLS, listTag);
        return tag;
    }

    private static CookingRequestBus load(CompoundTag tag) {
        CookingRequestBus bus = new CookingRequestBus();
        if (tag.contains(TAG_POOLS)) {
            ListTag listTag = tag.getList(TAG_POOLS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                RequestPool pool = RequestPool.fromNBT(listTag.getCompound(i));
                bus.pools.put(pool.restaurantId, pool);
            }
        }
        return bus;
    }

    public static CookingRequestBus get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CookingRequestBus::load,
                CookingRequestBus::new,
                "cooking_demand_bus"
        );
    }

    private static class RequestPool implements INBTSerializable<CompoundTag> {
        private static final String TAG_RESTAURANT_ID = "restaurant_id";
        private static final String TAG_ENTRIES = "entries";

        private String restaurantId;
        private List<Entry> entries;

        public RequestPool() {
            this.restaurantId = "";
            this.entries = new LinkedList<>();
        }

        public RequestPool(String restaurantId) {
            this.restaurantId = restaurantId;
            this.entries = new LinkedList<>();
        }

        /**
         * 入队一个请求
         * @param demand 将入队的请求
         */
        public void submit(CookingRequest demand) {
            entries.add(new Entry(demand));
        }

        /**
         * 从请求池认领一个请求,若有已认领的请求则不做处理
         * @param maidUUID 女仆的UUID
         * @param gameTime 时间戳
         * @return 新认领的请求
         */
        @Nullable
        public CookingRequest claim(UUID maidUUID, long gameTime) {
            Entry toClaim = null;
            for (var entry : entries) {
                if (entry.claimedBy == null) {
                    toClaim = entry;
                } else if (entry.claimedBy.equals(maidUUID)) {
                    return null;
                }
            }
            if (toClaim != null) {
                toClaim.claimedBy = maidUUID;
                toClaim.gameTime = gameTime;
            }
            return null;
        }

        /**
         * 释放当前认领的请求,并尝试从请求池重新认领另一个请求
         * @param maidUUID 女仆UUID
         * @param gameTime 时间戳
         * @return 新认领的请求
         */
        @Nullable
        public CookingRequest reclaim(UUID maidUUID, long gameTime) {
            int index = -1;
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                if (entry.claimedBy != null && entry.claimedBy.equals(maidUUID)) {
                    entry.claimedBy = null;
                    entry.gameTime = 0;
                    index = i;
                    break;
                }
            }

            int next = (index + 1) % entries.size();
            int attempts = 0;
            while (attempts < entries.size()) {
                var entry = entries.get(next);
                if (entry.claimedBy == null) {
                    entry.claimedBy = maidUUID;
                    entry.gameTime = gameTime;
                    return entry.request;
                }
                next = (next + 1) % entries.size();
                attempts++;
            }
            return null;
        }

        /**
         * 释放女仆已认领的请求
         * @param maidUUID 女仆UUID
         */
        public void release(UUID maidUUID) {
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maidUUID)) {
                    entry.claimedBy = null;
                    entry.gameTime = 0;
                    break;
                }
            }
        }

        /**
         * 出队一个已完成的请求
         * @param maidUUID 女仆UUID
         */
        public void complete(UUID maidUUID) {
            Entry completed = null;
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maidUUID)) {
                    completed = entry;
                    break;
                }
            }
            if (completed != null) {
                entries.remove(completed);
            }
        }

        /**
         * 获取女仆已认领的请求
         * @param maidUUID 女仆UUID
         * @return 返还已认领的请求
         */
        @Nullable
        public CookingRequest getClaimed(UUID maidUUID) {
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maidUUID)) {
                    return entry.request;
                }
            }
            return null;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_RESTAURANT_ID, restaurantId);
            ListTag listTag = new ListTag();
            for (var entry : entries) {
                listTag.add(entry.serializeNBT());
            }
            tag.put(TAG_ENTRIES, listTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains(TAG_RESTAURANT_ID)) {
                restaurantId = tag.getString(TAG_RESTAURANT_ID);
            }
            if (tag.contains(TAG_ENTRIES)) {
                ListTag listTag = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
                entries = new LinkedList<>();
                for (int i = 0; i < listTag.size(); i++) {
                    entries.add(Entry.fromNBT(listTag.getCompound(i)));
                }
            }
        }

        public static RequestPool fromNBT(CompoundTag tag) {
            RequestPool pool = new RequestPool();
            pool.deserializeNBT(tag);
            return pool;
        }

        private static class Entry implements INBTSerializable<CompoundTag> {
            private static final String TAG_REQUEST = "request";
            private static final String TAG_CLAIMED_BY = "claimed_by";
            private static final String TAG_GAME_TIME = "game_time";

            private CookingRequest request;
            @Nullable
            private UUID claimedBy;
            private long gameTime;

            private Entry() {}

            public Entry(CookingRequest request) {
                this.request = request;
            }

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                tag.put(TAG_REQUEST, request.serializeNBT());
                if (claimedBy != null) {
                    tag.putUUID(TAG_CLAIMED_BY, claimedBy);
                }
                tag.putLong(TAG_GAME_TIME, gameTime);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains(TAG_REQUEST)) {
                    this.request = CookingRequest.fromNBT(tag.getCompound(TAG_REQUEST));
                }
                if (tag.contains(TAG_CLAIMED_BY)) {
                    this.claimedBy = tag.getUUID(TAG_CLAIMED_BY);
                }
                if (tag.contains(TAG_GAME_TIME)) {
                    this.gameTime = tag.getLong(TAG_GAME_TIME);
                }
            }

            public static Entry fromNBT(CompoundTag tag) {
                Entry entry = new Entry();
                entry.deserializeNBT(tag);
                return entry;
            }
        }
    }
}
