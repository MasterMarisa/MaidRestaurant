package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.attachment.BlockSelection;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OrderMenuItem extends Item {
    public OrderMenuItem() { super(new Item.Properties().stacksTo(1)); }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player != null && !player.isSecondaryUseActive() && level.getBlockState(pos).is(TagBlock.SERVE_MEAL_BLOCK))
            return InteractionResult.FAIL;

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        BlockSelection selection = player.getData(BlockSelection.TYPE);
        if (!selection.selected.isEmpty()) {
            if (level.isClientSide()) {
                OrderingScreen.open(player,selection.selected);
            }
            player.removeData(BlockSelection.TYPE);
        }

        return super.use(level, player, usedHand);
    }
}
