package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.maid.task.TaskWaiter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class ServeRequestBus extends RequestBus<ServeRequest> {
    private static final Map<ServerLevel, ServeRequestBus> BUS_MAP = new ConcurrentHashMap<>();

    /**
     * 校验委托合法性
     */
    private boolean validate(ServerLevel level, String restaurantId, RequestEntry<ServeRequest> requestEntry) {
        if (requestEntry.owner == null) {
            return false;
        }
        if (!(level.getEntity(requestEntry.owner) instanceof EntityMaid maid)) {
            return false;
        }
        if (!(maid.getTask() instanceof TaskWaiter)) {
            return false;
        }
        if (requestEntry.request.targets.isEmpty()) {
            return false;
        }
        return true;
    }

    private static ServeRequestBus fromCompound(CompoundTag tag) {
        ServeRequestBus bus = new ServeRequestBus();
        bus.load(tag);
        return bus;
    }

    public static ServeRequestBus getInstance(ServerLevel level) {
        return BUS_MAP.computeIfAbsent(level, l ->
                l.getDataStorage().computeIfAbsent(
                        ServeRequestBus::fromCompound,
                        ServeRequestBus::new,
                        "serve_request_bus"));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 每15秒检测一次委托占有者的合法性，取最近的质数防止与其他任务的相位重叠
            if (level.getGameTime() % 293 != 0) {
                continue;
            }

            ServeRequestBus bus = getInstance(level);
            for (String key : bus.requestPool.keySet()) {
                List<RequestEntry<ServeRequest>> entries = bus.requestPool.get(key);
                List<RequestEntry<ServeRequest>> toRemove = new ArrayList<>();
                for (var entry : entries) {
                    if (!bus.validate(level, key, entry)) {
                        toRemove.add(entry);
                    }
                }

                if (!toRemove.isEmpty()) {
                    bus.setDirty();
                    for (var entry : toRemove) {
                        entries.remove(entry);
                    }
                }
            }
        }
    }
}
