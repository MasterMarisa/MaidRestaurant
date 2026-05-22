package com.mastermarisa.maid_restaurant.core.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class CookingRequestBus extends SavedData {
    private static final String TAG_POOLS = "pools";

    private final ConcurrentHashMap<String, RequestPool> pools;

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
        setDirty();
    }

    /**
     * 从请求池认领一个请求,若有已认领的请求则不做处理
     * @param restaurantId 餐厅Id
     * @param maid 女仆的
     * @param gameTime 时间戳
     * @return 新认领的请求
     */
    @Nullable
    public CookingRequest claim(String restaurantId, EntityMaid maid, long gameTime) {
        CookingRequest request = getPool(restaurantId).claim(maid, gameTime);
        if (request != null) {
            setDirty();
        }
        return request;
    }

    /**
     * 释放当前认领的请求,并尝试从请求池重新认领另一个请求
     * @param restaurantId 餐厅Id
     * @param maid 女仆
     * @param gameTime 时间戳
     * @return 新认领的请求
     */
    @Nullable
    public CookingRequest reclaim(String restaurantId, EntityMaid maid, long gameTime) {
        CookingRequest request = getPool(restaurantId).reclaim(maid, gameTime);
        setDirty();
        return request;
    }

    /**
     * 释放女仆已认领的请求
     * @param restaurantId 餐厅Id
     * @param maid 女仆
     */
    public void release(String restaurantId, EntityMaid maid) {
        if (getPool(restaurantId).release(maid)) {
            setDirty();
        }
    }

    /**
     * 出队一个已完成的请求
     * @param restaurantId 餐厅Id
     * @param maid 女仆
     */
    public void complete(String restaurantId, EntityMaid maid) {
        if (getPool(restaurantId).complete(maid)) {
            setDirty();
        }
    }

    /**
     * 获取女仆已认领的请求
     * @param restaurantId 餐厅Id
     * @param maid 女仆
     * @return 返还已认领的请求
     */
    @Nullable
    public CookingRequest getClaimed(String restaurantId, EntityMaid maid) {
        return getPool(restaurantId).getClaimed(maid);
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            long gameTime = level.getGameTime();
            // 每15秒检测一次合法性,取最近的质数减少并发
            if (gameTime % 293 == 0) {
                for (var pool : CookingRequestBus.get(level).pools.values()) {
                    pool.validate(level);
                }
            }
        }
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
         * @param maid 女仆
         * @param gameTime 时间戳
         * @return 新认领的请求
         */
        @Nullable
        public CookingRequest claim(EntityMaid maid, long gameTime) {
            Entry toClaim = null;
            for (var entry : entries) {
                if (entry.claimedBy == null) {
                    if (toClaim == null) {
                        toClaim = entry;
                    }
                } else if (entry.claimedBy.equals(maid.getUUID())) {
                    return null;
                }
            }
            if (toClaim != null) {
                toClaim.claimedBy = maid.getUUID();
                toClaim.gameTime = gameTime;
                return toClaim.request;
            }
            return null;
        }

        /**
         * 释放当前认领的请求,并尝试从请求池重新认领另一个请求
         * @param maid 女仆
         * @param gameTime 时间戳
         * @return 新认领的请求
         */
        @Nullable
        public CookingRequest reclaim(EntityMaid maid, long gameTime) {
            int index = -1;
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                if (entry.claimedBy != null && entry.claimedBy.equals(maid.getUUID())) {
                    entry.release();
                    index = i;
                    break;
                }
            }

            int next = (index + 1) % entries.size();
            int attempts = 0;
            while (attempts < entries.size()) {
                var entry = entries.get(next);
                if (entry.claimedBy == null) {
                    entry.claimedBy = maid.getUUID();
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
         * @param maid 女仆
         */
        public boolean release(EntityMaid maid) {
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maid.getUUID())) {
                    entry.release();
                    return true;
                }
            }
            return false;
        }

        /**
         * 出队一个已完成的请求
         * @param maid 女仆
         */
        public boolean complete(EntityMaid maid) {
            Entry completed = null;
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maid.getUUID())) {
                    completed = entry;
                    break;
                }
            }
            if (completed != null) {
                entries.remove(completed);
                return true;
            }
            return false;
        }

        /**
         * 获取女仆已认领的请求
         * @param maid 女仆
         * @return 返还已认领的请求
         */
        @Nullable
        public CookingRequest getClaimed(EntityMaid maid) {
            for (var entry : entries) {
                if (entry.claimedBy != null && entry.claimedBy.equals(maid.getUUID())) {
                    return entry.request;
                }
            }
            return null;
        }

        /**
         * 校验委托持有者的合法性,释放被非法占用的委托
         */
        public void validate(ServerLevel level) {
            for (var entry : entries) {
                if (entry.claimedBy == null) continue;
                if (!(level.getEntity(entry.claimedBy) instanceof EntityMaid maid)) {
                    entry.release();
                    continue;
                }
                ItemStack license = ChefScheduler.getChefLicense(maid);
                if (license.isEmpty()) {
                    entry.release();
                    continue;
                }
                String id = ChefLicenseItem.getRestaurantId(license);
                if (!id.equals(restaurantId)) {
                    entry.release();
                }
            }
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

            public void release() {
                claimedBy = null;
                gameTime = 0;
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
