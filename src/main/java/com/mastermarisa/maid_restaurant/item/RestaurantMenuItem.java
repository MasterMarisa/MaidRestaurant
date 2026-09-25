package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.blockentity.OrderBellBlockEntity;
import com.mastermarisa.maid_restaurant.client.gui.screen.RestaurantMenuScreen;
import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.schedule.CookingRequestBus;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.CodecUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class RestaurantMenuItem extends Item {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";
    private static final String TAG_ORDER_ENTRIES = "order_entries";
    private static final String TAG_TARGETS = "targets";
    private static final String TAG_SELECTING_TARGETS = "selecting_targets";

    public RestaurantMenuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }

        if (!isSelectingTargets(context.getItemInHand())) {
            return super.onItemUseFirst(stack, context);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) {
            setSelectingTargets(stack, false);
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            pos = pos.relative(direction);
        }

        List<ServeRequest.Target> targets = getTargets(stack);
        boolean removed = false;
        for (int i = 0; i < targets.size(); i++) {
            ServeRequest.Target target = targets.get(i);
            if (target.pos().equals(pos)) {
                targets.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            int type = StorageRegistry.tryGetAt(level, pos) != null ? 0 : 1;
            targets.add(new ServeRequest.Target(pos, type));
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

        if (isSelectingTargets(stack)) {
            setSelectingTargets(stack, false);
            if (player.isSecondaryUseActive()) {
                return InteractionResultHolder.success(stack);
            }
            setTargets(stack, List.of());
            return InteractionResultHolder.fail(stack);
        }

        if (isOrdering(stack)) {
            if (!level.isClientSide()) {
                removeOrderEntries(stack);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide()) {
            RestaurantMenuScreen.open(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemInHand = context.getItemInHand();

        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }

        if (isOrdering(itemInHand)) {
            if (level.getBlockEntity(pos) instanceof OrderBellBlockEntity be) {
                if (!level.isClientSide()) {
                    List<ServeRequest.Target> targets = new ArrayList<>(be.getTargets());
                    sendRequests(itemInHand, (ServerLevel) level, targets);
                }
                return InteractionResult.SUCCESS;
            }
            removeOrderEntries(itemInHand);
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    public static void sendRequests(ItemStack itemStack, ServerLevel serverLevel, @Nullable List<ServeRequest.Target> targets) {
        List<OrderEntry> entries = getOrderEntries(itemStack);

        if (targets == null) {
            targets = getTargets(itemStack);
        }

        for (OrderEntry entry : entries) {
            CookingRequest cookingRequest = new CookingRequest(entry.getEntry().getRoot());
            cookingRequest.root.applyCount(serverLevel, entry.getCount());

            if (!targets.isEmpty()) {
                ServeRequest serveRequest = new ServeRequest(cookingRequest.root.getIngredient(), entry.getCount());
                serveRequest.targets = targets;
                cookingRequest.boundRequest = serveRequest;
            }

            CookingRequestBus.getInstance(serverLevel).enqueue(getRestaurantId(itemStack), cookingRequest);
        }
        removeOrderEntries(itemStack);
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

    public static void removeOrderEntries(ItemStack itemStack) {
        itemStack.getOrCreateTag().remove(TAG_ORDER_ENTRIES);
    }

    public static boolean isOrdering(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains(TAG_ORDER_ENTRIES);
    }

    public static List<ServeRequest.Target> getTargets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        List<ServeRequest.Target> targets = new ArrayList<>();
        if (tag.contains(TAG_TARGETS)) {
            ListTag listTag = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                targets.add(CodecUtil.deserialize(listTag.getCompound(i), ServeRequest.Target.CODEC));
            }
        }
        return targets;
    }

    public static void setTargets(ItemStack itemStack, List<ServeRequest.Target> targets) {
        ListTag listTag = new ListTag();
        for (ServeRequest.Target target : targets) {
            listTag.add(CodecUtil.serialize(target, ServeRequest.Target.CODEC));
        }
        itemStack.getOrCreateTag().put(TAG_TARGETS, listTag);
    }

    public static boolean isSelectingTargets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_SELECTING_TARGETS)) {
            return tag.getBoolean(TAG_SELECTING_TARGETS);
        }
        return false;
    }

    public static void setSelectingTargets(ItemStack itemStack, boolean value) {
        itemStack.getOrCreateTag().putBoolean(TAG_SELECTING_TARGETS, value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return isOrdering(itemStack) || isSelectingTargets(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || isSelected) {
            return;
        }

        if (isOrdering(stack)) {
            removeOrderEntries(stack);
        }

        if (isSelectingTargets(stack)) {
            setTargets(stack, List.of());
            setSelectingTargets(stack, false);
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
