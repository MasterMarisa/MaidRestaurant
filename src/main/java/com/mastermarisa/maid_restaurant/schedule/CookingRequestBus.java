package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class CookingRequestBus extends RequestBus<CookingRequest> {
    private static final Map<ServerLevel, CookingRequestBus> BUS_MAP = new ConcurrentHashMap<>();

    /**
     * 校验委托合法性
     */
    private boolean validate(ServerLevel level, String restaurantId, RequestEntry<CookingRequest> requestEntry) {
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

    private static CookingRequestBus fromCompound(CompoundTag tag) {
        CookingRequestBus bus = new CookingRequestBus();
        bus.load(tag);
        return bus;
    }

    public static CookingRequestBus getInstance(ServerLevel level) {
        return BUS_MAP.computeIfAbsent(level, l ->
                l.getDataStorage().computeIfAbsent(
                        CookingRequestBus::fromCompound,
                        CookingRequestBus::new,
                        "cooking_request_bus"));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 每15秒检测一次委托占有者的合法性，取最近的质数防止与其他任务的相位重叠
            if (level.getGameTime() % 293 != 0) {
                continue;
            }
            CookingRequestBus bus = getInstance(level);
            for (String key : bus.requestPool.keySet()) {
                List<RequestEntry<CookingRequest>> entries = bus.requestPool.get(key);
                for (var entry : entries) {
                    if (!bus.validate(level, key, entry)) {
                        entry.release();
                        bus.setDirty();
                    }
                }
            }
        }
    }
}
