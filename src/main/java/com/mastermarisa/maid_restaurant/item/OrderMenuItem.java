package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.utils.component.BlockSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class OrderMenuItem extends Item {
    public OrderMenuItem() { super(new Item.Properties().stacksTo(1)); }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player != null && TaskWaiter.isValidServeBlock(level,pos))
            return InteractionResult.SUCCESS_NO_ITEM_USED;

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        BlockSelection selection = player.getData(BlockSelection.TYPE);
        if (!player.isSecondaryUseActive() && !selection.menu.isEmpty()) {
            if (level.isClientSide())
                OrderingScreen.open(player,new ArrayList<>(selection.menu));
            selection.menu.clear();
            player.setData(BlockSelection.TYPE,selection);
        }

        return super.use(level, player, usedHand);
    }
}
