package com.mastermarisa.maid_restaurant.core.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class CookingRequestBus extends SavedData {
    private static final String TAG_POOLS = "pools";
    private static final String TAG_RESTAURANT_ID = "restaurant_id";
    private static final String TAG_ENTRIES = "entries";

    private final ConcurrentHashMap<String, List<Entry>> pools;

    public CookingRequestBus() {
        pools = new ConcurrentHashMap<>();
    }

    /**
     * 向请求池中添加一个新的委托
     * @param restaurantId 餐厅Id
     * @param request 要添加的烹饪请求
     */
    public void add(String restaurantId, CookingRequest request) {
        getEntries(restaurantId).add(new Entry(request));
        setDirty();
    }

    /**
     * 认领一个未被占用的委托
     * @param restaurantId 餐厅Id
     * @param level 所在世界
     * @param maid 女仆实体
     * @return 新认领的委托，如果没有可用委托则返回 null
     */
    @Nullable
    public CookingRequest claim(String restaurantId, ServerLevel level, EntityMaid maid) {
        List<Entry> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index == -1) {
            for (var entry : entries) {
                if (entry.claimedBy == null) {
                    entry.claim(level, maid);
                    setDirty();
                    return entry.request;
                }
            }
        }
        return null;
    }

    /**
     * 释放当前女仆占用的委托，并尝试认领另一个未被占用的委托
     * @param restaurantId 餐厅Id
     * @param level 所在世界
     * @param maid 女仆实体
     * @return 新认领的委托
     */
    @Nullable
    public CookingRequest reclaim(String restaurantId, ServerLevel level, EntityMaid maid) {
        List<Entry> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index != -1) {
            entries.get(index).release(level);
        }
        index = (index + 1) % entries.size();
        int attempts = 0;
        while (attempts < entries.size()) {
            Entry entry = entries.get(index);
            if (entry.claimedBy == null) {
                entry.claim(level, maid);
                setDirty();
                return entry.request;
            }
            index = (index + 1) % entries.size();
            attempts++;
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
    public CookingRequest getClaimed(String restaurantId, EntityMaid maid) {
        List<Entry> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        return index != -1 ? entries.get(index).request : null;
    }

    /**
     * 释放当前女仆占用的委托
     * @param restaurantId 餐厅Id
     * @param level 所在世界
     * @param maid 女仆实体
     * @return 如果成功释放则返回 true，否则返回 false
     */
    public boolean release(String restaurantId, ServerLevel level, EntityMaid maid) {
        List<Entry> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index != -1) {
            entries.get(index).release(level);
            setDirty();
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
    public boolean submit(String restaurantId, EntityMaid maid) {
        List<Entry> entries = getEntries(restaurantId);
        int index = findEntryIndex(entries, maid);
        if (index != -1) {
            entries.remove(index);
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * 根据女仆 UUID 查找其在 entries 列表中的索引位置
     * @param maid 女仆实体
     * @return 索引值，如果未找到则返回 -1
     */
    private int findEntryIndex(List<Entry> entries, EntityMaid maid) {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.claimedBy != null && entry.claimedBy.equals(maid.getUUID())) {
                return i;
            }
        }
        return -1;
    }

    private List<Entry> getEntries(String restaurantId) {
        return pools.computeIfAbsent(restaurantId, k -> new LinkedList<>());
    }

    public static CookingRequestBus get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CookingRequestBus::load,
                CookingRequestBus::new,
                "cooking_demand_bus"
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (var entry : pools.entrySet()) {
            CompoundTag poolTag = new CompoundTag();
            poolTag.putString(TAG_RESTAURANT_ID, entry.getKey());
            ListTag entriesTag = new ListTag();
            for (Entry e : entry.getValue()) {
                entriesTag.add(e.serializeNBT());
            }
            poolTag.put(TAG_ENTRIES, entriesTag);
            listTag.add(poolTag);
        }
        tag.put(TAG_POOLS, listTag);
        return tag;
    }

    private static CookingRequestBus load(CompoundTag tag) {
        CookingRequestBus bus = new CookingRequestBus();
        if (tag.contains(TAG_POOLS)) {
            ListTag listTag = tag.getList(TAG_POOLS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag poolTag = listTag.getCompound(i);
                String restaurantId = poolTag.getString(TAG_RESTAURANT_ID);
                List<Entry> entries = new LinkedList<>();
                ListTag entriesTag = poolTag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
                for (int j = 0; j < entriesTag.size(); j++) {
                    entries.add(Entry.fromNBT(entriesTag.getCompound(j)));
                }
                bus.pools.put(restaurantId, entries);
            }
        }
        return bus;
    }

    private static class Entry implements INBTSerializable<CompoundTag> {
        private static final String TAG_REQUEST = "request";
        private static final String TAG_CLAIMED_BY = "claimed_by";
        private static final String TAG_LAST_CLAIMED_TIME = "last_claimed_time";
        private static final String TAG_LAST_RELEASED_TIME = "last_released_time";

        private CookingRequest request;
        @Nullable
        private UUID claimedBy;
        private long lastClaimedTime;
        private long lastReleasedTime;

        public Entry() {
            this.lastClaimedTime = -1;
            this.lastReleasedTime = -1;
        }

        public Entry(CookingRequest request) {
            this.request = request;
            this.lastClaimedTime = -1;
            this.lastReleasedTime = -1;
        }

        public void claim(ServerLevel level, EntityMaid maid) {
            this.request.getRoot().verifyAndUpdateState(maid);
            this.claimedBy = maid.getUUID();
            this.lastClaimedTime = level.getGameTime();
        }

        public void release(ServerLevel level) {
            this.claimedBy = null;
            this.lastReleasedTime = level.getGameTime();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put(TAG_REQUEST, request.serializeNBT());
            if (claimedBy != null) {
                tag.putUUID(TAG_CLAIMED_BY, claimedBy);
            }
            tag.putLong(TAG_LAST_CLAIMED_TIME, lastClaimedTime);
            tag.putLong(TAG_LAST_RELEASED_TIME, lastReleasedTime);
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
            if (tag.contains(TAG_LAST_CLAIMED_TIME)) {
                this.lastClaimedTime = tag.getLong(TAG_LAST_CLAIMED_TIME);
            }
            if (tag.contains(TAG_LAST_RELEASED_TIME)) {
                this.lastReleasedTime = tag.getLong(TAG_LAST_RELEASED_TIME);
            }
        }

        public static Entry fromNBT(CompoundTag tag) {
            Entry entry = new Entry();
            entry.deserializeNBT(tag);
            return entry;
        }
    }
}
