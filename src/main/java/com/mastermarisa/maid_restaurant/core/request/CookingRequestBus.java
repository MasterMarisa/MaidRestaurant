package com.mastermarisa.maid_restaurant.core.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;
import com.mastermarisa.maid_restaurant.uitls.ChefScheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CookingRequestBus extends RequestBus<CookingRequest> {
    private static final Map<ServerLevel, CookingRequestBus> BUS_MAP = new ConcurrentHashMap<>();

    public CookingRequestBus() {
        super(CookingRequest::fromNBT);
    }

    public static CookingRequestBus getInstance(ServerLevel level) {
        return BUS_MAP.computeIfAbsent(level, l -> l.getDataStorage().computeIfAbsent(
                CookingRequestBus::fromNBT,
                CookingRequestBus::new,
                "cooking_request_bus"
        ));
    }

    private static CookingRequestBus fromNBT(CompoundTag tag) {
        CookingRequestBus bus = new CookingRequestBus();
        bus.load(tag);
        return bus;
    }

    @Override
    protected boolean isOwnerValid(ServerLevel level, String restaurantId, CookingRequest request, UUID owner) {
        if (!(level.getEntity(owner) instanceof EntityMaid maid)) {
            return false;
        }
        if (!(maid.getTask() instanceof TaskChef)) {
            return false;
        }
        ItemStack itemStack = ChefScheduler.getChefLicense(maid);
        if (itemStack.isEmpty()) {
            return false;
        }
        return restaurantId.equals(ChefLicenseItem.getRestaurantId(itemStack));
    }

    @Override
    protected boolean isRequestValid(ServerLevel level, String restaurantId, CookingRequest request) {
        return true;
    }

    @Override
    public void unload(ServerLevel level) {
        BUS_MAP.remove(level);
    }
}
