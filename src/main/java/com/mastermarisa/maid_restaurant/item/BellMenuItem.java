package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.blockentity.OrderBellBlockEntity;
import com.mastermarisa.maid_restaurant.client.gui.screen.BellMenuScreen;
import com.mastermarisa.maid_restaurant.core.request.CookingRequestBus;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.world.LevelRecipeLookup;
import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.core.request.CookingRequest;
import com.mastermarisa.maid_restaurant.core.request.ServingRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BellMenuItem extends Item {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";
    private static final String TAG_ORDER_ENTRIES = "order_entries";

    public BellMenuItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (level.isClientSide() && !tag.contains(TAG_ORDER_ENTRIES)) {
            BellMenuScreen.open(stack);
        }

        if (tag.contains(TAG_ORDER_ENTRIES)) {
            if (player instanceof ServerPlayer serverPlayer) {
                tag.remove(TAG_ORDER_ENTRIES);
                MutableComponent component = Component.literal("请对点餐铃使用!").withStyle(ChatFormatting.RED);
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(component));
            }
            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }


        CompoundTag tag = itemStack.getOrCreateTag();
        if (!tag.contains(TAG_ORDER_ENTRIES)) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof OrderBellBlockEntity be)) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                tag.remove(TAG_ORDER_ENTRIES);
                MutableComponent component = Component.literal("请对点餐铃使用!").withStyle(ChatFormatting.RED);
                player.connection.send(new ClientboundSetActionBarTextPacket(component));
            }
            return InteractionResult.FAIL;
        }

        if (context.getPlayer() instanceof ServerPlayer player) {
            List<ServingRequest.Target> targets = be.getTargets();
            List<OrderEntry> entries = getOrderEntries(itemStack);
            for (OrderEntry order : entries) {
                RecipeNode root = order.getEntry().getRoot().copy();
                CookingRequest request = new CookingRequest(root);
                request.root.applyCount(LevelRecipeLookup.of(level), order.getCount());
                if (!targets.isEmpty()) {
                    request.boundRequest = new ServingRequest(root.getIngredient(), order.getCount());
                    request.boundRequest.targets = new ArrayList<>(targets);
                }
                CookingRequestBus.getInstance((ServerLevel) level).enqueue(getRestaurantId(itemStack), request);
            }
            tag.remove(TAG_ORDER_ENTRIES);
            MutableComponent component = Component.literal("下单成功!").withStyle(ChatFormatting.GREEN);
            player.connection.send(new ClientboundSetActionBarTextPacket(component));
        }
        return InteractionResult.SUCCESS;
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

    public static List<OrderEntry> getOrderEntries(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        List<OrderEntry> entries = new ArrayList<>();
        if (tag.contains(TAG_ORDER_ENTRIES)) {
            ListTag listTag = tag.getList(TAG_ORDER_ENTRIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                entries.add(OrderEntry.fromNBT(listTag.getCompound(i)));
            }
        }
        return entries;
    }

    public static void setOrderEntries(ItemStack itemStack, List<OrderEntry> entries) {
        ListTag listTag = new ListTag();
        for (OrderEntry entry : entries) {
            listTag.add(entry.serializeNBT());
        }
        itemStack.getOrCreateTag().put(TAG_ORDER_ENTRIES, listTag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains(TAG_ORDER_ENTRIES);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || isSelected) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_ORDER_ENTRIES)) {
            tag.remove(TAG_ORDER_ENTRIES);
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
