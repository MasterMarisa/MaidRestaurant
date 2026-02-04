package com.mastermarisa.maid_restaurant.events;

import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.attachment.BlockSelection;
import com.mastermarisa.maid_restaurant.init.InitItems;
import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import java.util.Arrays;

public class BlockSelector {
    @SubscribeEvent
    public static void useItemOnBlock(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (!level.isClientSide() && player != null && !player.isSecondaryUseActive() && itemStack.is(InitItems.ORDER_MENU)) {
            BlockSelection selection = player.getData(BlockSelection.TYPE);
            if (Arrays.stream(BlockPosUtils.pack(selection.selected)).anyMatch(l->l==pos.asLong())) {
                selection.selected.remove(pos);
                player.setData(BlockSelection.TYPE,selection);
                player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_removed").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                event.setCanceled(true);
                event.setCancellationResult(ItemInteractionResult.SUCCESS);
            } else {
                if (level.getBlockState(pos).is(TagBlock.SERVE_MEAL_BLOCK) && level.getBlockState(pos.above()).canBeReplaced()) {
                    selection.selected.add(pos);
                    player.setData(BlockSelection.TYPE,selection);
                    player.sendSystemMessage(Component.translatable("message.maid_restaurant.block_selected").append(":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                    event.setCanceled(true);
                    event.setCancellationResult(ItemInteractionResult.SUCCESS);
                }
            }
        }
    }
}
