package com.mastermarisa.maid_restaurant.event;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.utils.BehaviorUtils;
import com.mastermarisa.maid_restaurant.utils.BlockUsageManager;
import com.mastermarisa.maid_restaurant.utils.component.BlockSelection;
import com.simibubi.create.foundation.utility.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@EventBusSubscriber
public class BlockSelector {
    @SubscribeEvent
    public static void useItemOnBlock(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (level.isClientSide() || player == null) return;

        BlockSelection selection = player.getData(BlockSelection.TYPE);
        if (itemStack.is(ModItems.ORDER_MENU)) {
            if (selection.menu.stream().anyMatch(l -> l.asLong() == pos.asLong())) {
                selection.menu.remove(pos);
                player.setData(BlockSelection.TYPE,selection);
                player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_removed").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                event.setCanceled(true);
                event.setCancellationResult(ItemInteractionResult.SUCCESS);
                player.getCooldowns().addCooldown(itemStack.getItem(),2);
            } else {
                if (TaskWaiter.isValidServeBlock(level,pos)) {
                    selection.menu.add(pos);
                    player.setData(BlockSelection.TYPE,selection);
                    player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_selected").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                    event.setCanceled(true);
                    event.setCancellationResult(ItemInteractionResult.SUCCESS);
                    player.getCooldowns().addCooldown(itemStack.getItem(),2);
                }
            }
        } else if (itemStack.is(ModItems.ORDER_ITEM)) {
            MaidRestaurant.LOGGER.debug("USER:" + BlockUsageManager.getUserCount(pos));
            event.setCanceled(true);
            event.setCancellationResult(ItemInteractionResult.SUCCESS);
//            if (selection.order.stream().anyMatch(l -> l.asLong() == pos.asLong())) {
//                selection.order.remove(pos);
//                player.setData(BlockSelection.TYPE,selection);
//                player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_removed").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
//                event.setCanceled(true);
//                event.setCancellationResult(ItemInteractionResult.SUCCESS);
//                player.getCooldowns().addCooldown(itemStack.getItem(),2);
//            } else {
//                if (TaskWaiter.isValidServeBlock(level,pos)) {
//                    selection.order.add(pos);
//                    player.setData(BlockSelection.TYPE,selection);
//                    player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_selected").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
//                    event.setCanceled(true);
//                    event.setCancellationResult(ItemInteractionResult.SUCCESS);
//                    player.getCooldowns().addCooldown(itemStack.getItem(),2);
//                }
//            }
        }
    }
}
