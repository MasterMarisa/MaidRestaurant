package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.core.zone.PointSetZone;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class PointSetZoneDefinitionItem extends Item {
    private static final String TAG_ZONE = "zone";

    public PointSetZoneDefinitionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        PointSetZone zone = new PointSetZone();
        zone.add(pos);
        setZone(stack, zone);
        player.sendSystemMessage(Component.literal("位置已添加:" + pos.toShortString()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Nullable
    public static PointSetZone getZone(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_ZONE)) {
            return PointSetZone.fromNBT(tag.getCompound(TAG_ZONE));
        }
        return null;
    }

    public static void setZone(ItemStack itemStack, PointSetZone zone) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put(TAG_ZONE, zone.serializeNBT());
    }
}
