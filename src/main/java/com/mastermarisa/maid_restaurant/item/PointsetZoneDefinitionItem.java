package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.core.zone.PointsetZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class PointsetZoneDefinitionItem extends ZoneDefinitionItem {
    public PointsetZoneDefinitionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        if (context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            pos = pos.relative(direction);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        PointsetZone zone = (PointsetZone) getZone(stack);
        if (zone == null) {
            zone = new PointsetZone();
        }
        if (zone.contains(pos)) {
            zone.remove(pos);
        } else {
            zone.add(pos);
        }
        setZone(stack, zone);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
