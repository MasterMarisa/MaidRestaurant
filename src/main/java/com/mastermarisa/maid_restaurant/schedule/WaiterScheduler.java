package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class WaiterScheduler {
    @Nullable
    public static ServeRequest getOrClaimRequest(ServerLevel level, EntityMaid maid) {
        ServeRequestBus bus = ServeRequestBus.getInstance(level);
        ServeRequest request = bus.getClaimed("chef", maid);
        if (request == null) {
            request = bus.claim("chef", maid);
        }
        return request;
    }
}
