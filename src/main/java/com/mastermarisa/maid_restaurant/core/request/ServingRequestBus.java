package com.mastermarisa.maid_restaurant.core.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.maid.task.TaskWaiter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServingRequestBus extends RequestBus<ServingRequest> {
    private static final Map<ServerLevel, ServingRequestBus> BUS_MAP = new ConcurrentHashMap<>();

    public ServingRequestBus() {
        super(ServingRequest::fromNBT);
    }

    public static ServingRequestBus getInstance(ServerLevel level) {
        return BUS_MAP.computeIfAbsent(level, l -> l.getDataStorage().computeIfAbsent(
                ServingRequestBus::fromNBT,
                ServingRequestBus::new,
                "serving_request_bus"
        ));
    }

    private static ServingRequestBus fromNBT(CompoundTag tag) {
        ServingRequestBus bus = new ServingRequestBus();
        bus.load(tag);
        return bus;
    }

    @Override
    protected boolean isOwnerValid(ServerLevel level, String restaurantId, ServingRequest request, UUID owner) {
        if (!(level.getEntity(owner) instanceof EntityMaid maid)) {
            return false;
        }
        return maid.getTask() instanceof TaskWaiter;
    }

    @Override
    protected boolean isRequestValid(ServerLevel level, String restaurantId, ServingRequest request) {
        return !request.targets.isEmpty();
    }

    @Override
    public void unload(ServerLevel level) {
        BUS_MAP.remove(level);
    }
}
