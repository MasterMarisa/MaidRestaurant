package com.mastermarisa.maid_restaurant.events;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTaskEnableEvent;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import net.neoforged.bus.api.SubscribeEvent;

public class OnMaidTaskEnable {
    @SubscribeEvent
    public static void onMaidTaskEnable(MaidTaskEnableEvent event) {
        if (event.getEntityMaid().getTask() instanceof TaskCooker) {
            BehaviorUtils.eraseTargetPos(event.getEntityMaid());
        }
    }
}
