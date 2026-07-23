package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;
import com.mastermarisa.maid_restaurant.uitls.SerializerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class RequestBus<T extends INBTSerializable<CompoundTag>> extends SavedData {
    private static final ResourceLocation COOKING_REQUEST = MaidRestaurant.modLoc("cooking_request");
    private static final ResourceLocation SERVE_REQUEST = MaidRestaurant.modLoc("serve_request");
    private static final SerializerRegistry<INBTSerializable<CompoundTag>> REGISTRY = new SerializerRegistry<>();
    private static final Map<ServerLevel, Map<ResourceLocation, RequestBus<?>>> BUS_MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Supplier<RequestBus<?>>> SUPPLIER_MAP = new ConcurrentHashMap<>();

    private final Map<String, List<RequestEntry<T>>> requestPool = new ConcurrentHashMap<>();

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

    /**
     * 校验委托合法性
     */
    private boolean validate(ServerLevel level, String restaurantId, RequestEntry<T> requestEntry) {
        if (requestEntry.owner == null) {
            return false;
        }
        if (!(level.getEntity(requestEntry.owner) instanceof EntityMaid maid)) {
            return false;
        }
        if (!(maid.getTask() instanceof TaskChef)) {
            return false;
        }
        ItemStack license = ChefScheduler.getChefLicense(maid);
        if (license.isEmpty()) {
            return false;
        }
        return restaurantId.equals(ChefLicenseItem.getRestaurantId(license));
    }

    private void validateAll(ServerLevel level) {
        for (var poolEntry : requestPool.entrySet()) {
            List<RequestEntry<T>> toRemove = new ArrayList<>();
            for (var entry : poolEntry.getValue()) {
                if (!validate(level, poolEntry.getKey(), entry)) {
                    setDirty();
                    entry.release();
                    if (entry.request instanceof ServeRequest) {
                        toRemove.add(entry);
                    }
                }
            }

            for (RequestEntry<T> tRequestEntry : toRemove) {
                poolEntry.getValue().remove(tRequestEntry);
            }
            setDirty();
        }
    }

    private int findEntryIndex(List<RequestEntry<T>> entries, EntityMaid maid) {
        for (int i = 0; i < entries.size(); i++) {
            RequestEntry<T> entry = entries.get(i);
            if (entry.owner != null && entry.owner.equals(maid.getUUID())) {
                return i;
            }
        }
        return -1;
    }

    private List<RequestEntry<T>> getEntries(String restaurantId) {
        return requestPool.computeIfAbsent(restaurantId, k -> new ArrayList<>());
    }

    public static void registerSerializers() {
        REGISTRY.register(COOKING_REQUEST, CookingRequest.class, CookingRequest::fromNBT);
        REGISTRY.register(SERVE_REQUEST, ServeRequest.class, ServeRequest::fromNBT);
    }

    @SuppressWarnings("unchecked")
    private static <T extends INBTSerializable<CompoundTag>> T deserializeRequest(CompoundTag tag) {
        return (T) REGISTRY.deserialize(tag);
    }

    private static <T extends INBTSerializable<CompoundTag>> RequestBus<T> load(CompoundTag tag) {
        RequestBus<T> bus = new RequestBus<>();
        if (tag.contains("pool")) {
            ListTag poolTag = tag.getList("pool", Tag.TAG_COMPOUND);
            for (int i = 0; i < poolTag.size(); i++) {
                CompoundTag entryTag = poolTag.getCompound(i);
                String restaurantId = entryTag.getString("restaurant_id");
                List<RequestEntry<T>> entryList = new LinkedList<>();
                for (var request : entryTag.getList("entry_list", Tag.TAG_COMPOUND)) {
                    entryList.add(RequestEntry.fromNBT((CompoundTag) request));
                }
                bus.requestPool.put(restaurantId, entryList);
            }
        }
        return bus;
    }

    @SuppressWarnings("unchecked")
    public static <T extends INBTSerializable<CompoundTag>> RequestBus<T> getInstance(ServerLevel level, Class<T> clazz) {
        ResourceLocation key = REGISTRY.keyByClass.get(clazz);
        Map<ResourceLocation, RequestBus<?>> map = BUS_MAP.computeIfAbsent(level, k -> new ConcurrentHashMap<>());
        RequestBus<?> bus = map.computeIfAbsent(key, k ->
                level.getDataStorage().computeIfAbsent(
                        RequestBus::load,
                        RequestBus::new,
                        key.toString()
                )
        );
        return (RequestBus<T>) bus;
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

    private static class RequestEntry<T extends INBTSerializable<CompoundTag>> implements INBTSerializable<CompoundTag> {
        private T request;
        @Nullable
        private UUID owner;

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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 每15秒检测一次委托占有者的合法性，取最近的质数防止与其他任务的相位重叠
            if (level.getGameTime() % 293 != 0) {
                continue;
            }
            for (var key : REGISTRY.keyByClass.keySet()) {
                getInstance(level, key).validateAll(level);
            }
        }
    }
}
