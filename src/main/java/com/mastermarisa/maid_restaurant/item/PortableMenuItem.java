package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.PortableMenuScreen;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.data.request.ServingRequest;
import com.mastermarisa.maid_restaurant.uitls.CodecUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PortableMenuItem extends Item {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";
    private static final String TAG_TARGETS = "targets";

    public PortableMenuItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();

        if (direction == Direction.UP || direction == Direction.DOWN) {
            pos = pos.relative(direction);
        }

        List<ServingRequest.Target> targets = getTargets(stack);
        boolean removed = false;
        for (int i = 0; i < targets.size(); i++) {
            ServingRequest.Target target = targets.get(i);
            if (target.pos().equals(pos)) {
                targets.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            int type = StorageRegistry.tryGetAt(level, pos) != null ? 0 : 1;
            targets.add(new ServingRequest.Target(pos, type));
        }

        setTargets(stack, targets);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide()) {
            PortableMenuScreen.open(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static List<ServingRequest.Target> getTargets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        List<ServingRequest.Target> targets = new ArrayList<>();
        if (tag.contains(TAG_TARGETS)) {
            ListTag listTag = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                targets.add(CodecUtil.deserialize(listTag.getCompound(i), ServingRequest.Target.CODEC));
            }
        }
        return targets;
    }

    public static void setTargets(ItemStack itemStack, List<ServingRequest.Target> targets) {
        ListTag listTag = new ListTag();
        for (ServingRequest.Target target : targets) {
            listTag.add(CodecUtil.serialize(target, ServingRequest.Target.CODEC));
        }
        itemStack.getOrCreateTag().put(TAG_TARGETS, listTag);
    }

    public static void removeTargets(ItemStack itemStack) {
        itemStack.getOrCreateTag().remove(TAG_TARGETS);
    }

    public static String getRestaurantId(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_RESTAURANT_ID)) {
            return tag.getString(TAG_RESTAURANT_ID);
        }
        return "";
    }

    public static void setRestaurantId(ItemStack itemStack, String restaurantId) {
        itemStack.getOrCreateTag().putString(TAG_RESTAURANT_ID, restaurantId);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || isSelected) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_TARGETS)) {
            tag.remove(TAG_TARGETS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        String restaurantId = getRestaurantId(stack);
        if (!restaurantId.isEmpty()) {
            Component component = Component.literal(restaurantId).withStyle(ChatFormatting.GRAY);
            tooltip.add(component);
        }
    }
}
