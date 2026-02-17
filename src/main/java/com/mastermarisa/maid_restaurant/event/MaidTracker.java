package com.mastermarisa.maid_restaurant.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.utils.BehaviorUtils;
import com.mastermarisa.maid_restaurant.utils.BlockUsageManager;
import com.mastermarisa.maid_restaurant.utils.EncodeUtils;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EventBusSubscriber
public class MaidTracker {
    public static final List<EntityMaid> maids = new ArrayList<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity().getType() == EntityMaid.TYPE) {
            EntityMaid maid = (EntityMaid) event.getEntity();
            maids.add(maid);

            if (maid.getTask() instanceof TaskWaiter) {
                maid.getData(ServeRequestHandler.TYPE).toList().forEach(serveRequest -> {
                    serveRequest.targets.forEach(p -> BlockUsageManager.addUser(p,maid.getUUID()));
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity().getType() == EntityMaid.TYPE) {
            EntityMaid maid = (EntityMaid) event.getEntity();
            maids.remove(maid);

            BehaviorUtils.eraseTargetPos(maid);

            if (maid.getTask() instanceof TaskCook) {
                CookRequest cookRequest = (CookRequest) RequestManager.peek(maid,CookRequest.TYPE);
                if (cookRequest != null)
                    Arrays.stream(cookRequest.targets).forEach(l -> BlockUsageManager.removeUser(EncodeUtils.decode(l),maid.getUUID()));
            }

            if (maid.getTask() instanceof TaskWaiter) {
                maid.getData(ServeRequestHandler.TYPE).toList().forEach(serveRequest -> {
                    serveRequest.targets.forEach(p -> BlockUsageManager.removeUser(p,maid.getUUID()));
                });
            }
        }
    }
}
