package com.mastermarisa.maid_restaurant.events;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;

public class MaidTracker {
    public static final List<EntityMaid> maids = new ArrayList<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity().getType() == EntityMaid.TYPE) {
            EntityMaid maid = (EntityMaid) event.getEntity();
            maids.add(maid);
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity().getType() == EntityMaid.TYPE) {
            EntityMaid maid = (EntityMaid) event.getEntity();
            maids.remove(maid);
            BehaviorUtils.eraseTargetPos(maid);
            BehaviorUtils.eraseChairPos(maid);
        }
    }
}
