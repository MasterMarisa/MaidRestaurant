package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.RestaurantMenuScreen;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RestaurantMenuItem extends Item {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";

    public RestaurantMenuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide()) {
            RestaurantMenuScreen.open(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static Map<Integer, MenuEntry> getEntries(ItemStack itemStack) {
        return UnboundMenuItem.getEntries(itemStack);
    }

    public static void setEntries(ItemStack itemStack, Map<Integer, MenuEntry> map) {
        UnboundMenuItem.setEntries(itemStack, map);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        String restaurantId = getRestaurantId(stack);
        if (!restaurantId.isEmpty()) {
            Component component = Component.literal("-" + restaurantId).withStyle(ChatFormatting.GRAY);
            tooltip.add(component);
        }
    }
}
