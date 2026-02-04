package com.mastermarisa.maid_restaurant.events;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.client.gui.screen.serve_request.ServeRequestScreen;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mastermarisa.maid_restaurant.init.InitItems;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.task.TaskWaiter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class OnEntityInteract {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (target instanceof EntityMaid maid && player.getMainHandItem().is(InitItems.ORDER_MENU)) {
             if (maid.getTask() instanceof TaskCooker && maid.getData(CookRequestQueue.TYPE).size() > 0) {
                 if (level.isClientSide()) {
                     CookRequestScreen.open(maid);
                 }
                 event.setCanceled(true);
                 event.setCancellationResult(InteractionResult.SUCCESS);
             } else if (maid.getTask() instanceof TaskWaiter && maid.getData(ServeRequestQueue.TYPE).size() > 0) {
                 if (level.isClientSide()) {
                     ServeRequestScreen.open(maid);
                 }
                 event.setCanceled(true);
                 event.setCancellationResult(InteractionResult.SUCCESS);
             }
        }
    }
}
