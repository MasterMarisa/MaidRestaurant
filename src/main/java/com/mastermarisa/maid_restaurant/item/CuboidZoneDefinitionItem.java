package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.core.zone.CuboidZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CuboidZoneDefinitionItem extends Item {
    private static final String TAG_ZONE = "zone";
    private static final String TAG_TEMP_VERTEX = "temp_vertex";

    public CuboidZoneDefinitionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            pos = pos.relative(direction);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos tempVertex = getTempVertex(stack);
        if (tempVertex == null) {
            setTempVertex(stack, pos);
            return InteractionResult.SUCCESS;
        }

        if (tempVertex.equals(pos)) {
            clearTempVertex(stack);
            player.sendSystemMessage(Component.literal("§c顶点重叠!"));
            return InteractionResult.SUCCESS;
        }

        if (tempVertex.distSqr(pos) > 102400) {
            clearTempVertex(stack);
            player.sendSystemMessage(Component.literal("§c选区过大!"));
            return InteractionResult.SUCCESS;
        }

        CuboidZone zone = new CuboidZone(tempVertex, pos);
        setZone(stack, zone);
        clearTempVertex(stack);
        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CuboidZone zone = getZone(stack);
        if (zone != null) {
            tooltipComponents.add(Component.literal(zone.toString()));
        }
    }

    @Nullable
    public static CuboidZone getZone(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_ZONE)) {
            return CuboidZone.fromNBT(tag.getCompound(TAG_ZONE));
        }
        return null;
    }

    public static void setZone(ItemStack itemStack, CuboidZone zone) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put(TAG_ZONE, zone.serializeNBT());
    }

    @Nullable
    public static BlockPos getTempVertex(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_TEMP_VERTEX)) {
            return BlockPos.of(tag.getLong(TAG_TEMP_VERTEX));
        }
        return null;
    }

    private static void setTempVertex(ItemStack itemStack, BlockPos pos) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putLong(TAG_TEMP_VERTEX, pos.asLong());
    }

    private static void clearTempVertex(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.remove(TAG_TEMP_VERTEX);
    }
}
