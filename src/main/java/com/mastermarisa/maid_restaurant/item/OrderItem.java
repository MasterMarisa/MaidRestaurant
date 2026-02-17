package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.init.ModDataComponents;
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

public class OrderItem extends Item {
    public OrderItem() { super(new Item.Properties().stacksTo(1)); }

    public static boolean hasRequests(ItemStack stack) {
        return stack.has(ModDataComponents.RECORDED_COOK_REQUESTS);
    }

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
        if (!player.isSecondaryUseActive() && !selection.order.isEmpty()) {
            selection.order.clear();
            player.setData(BlockSelection.TYPE,selection);
        }

        return super.use(level, player, usedHand);
    }
}
