package com.mastermarisa.maid_restaurant.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.network.OpenScreenPayload;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class MaidScreenOpening {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) return;

        if (target instanceof EntityMaid maid && player.getMainHandItem().is(ModItems.ORDER_MENU)) {
             CookRequestHandler cookRequestHandler = maid.getData(CookRequestHandler.TYPE);
             ServeRequestHandler serveRequestHandler = maid.getData(ServeRequestHandler.TYPE);
             if (maid.getTask() instanceof TaskCook) {
                 maid.removeData(CookRequestHandler.TYPE);
                 maid.setData(CookRequestHandler.TYPE,cookRequestHandler);
                 OpenScreenPayload payload = new OpenScreenPayload(0,maid.getId());
                 PacketDistributor.sendToPlayer((ServerPlayer) player,payload);
                 event.setCanceled(true);
                 event.setCancellationResult(InteractionResult.SUCCESS);
             }
             else if (maid.getTask() instanceof TaskWaiter && serveRequestHandler.size() > 0) {
                 maid.removeData(ServeRequestHandler.TYPE);
                 maid.setData(ServeRequestHandler.TYPE,serveRequestHandler);
                 OpenScreenPayload payload = new OpenScreenPayload(1,maid.getId());
                 PacketDistributor.sendToPlayer((ServerPlayer) player,payload);
                 event.setCanceled(true);
                 event.setCancellationResult(InteractionResult.SUCCESS);
             }
        }
    }
}
