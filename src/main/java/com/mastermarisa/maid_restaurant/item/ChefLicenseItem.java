package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.inventory.ZoneDefinitionHandler;
import com.mastermarisa.maid_restaurant.inventory.container.ChefLicenseContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class ChefLicenseItem extends Item implements MenuProvider {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";
    private static final String TAG_INVENTORY = "inventory";

    public ChefLicenseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, buf -> buf.writeItem(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static String getRestaurantId(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_RESTAURANT_ID)) {
            return tag.getString(TAG_RESTAURANT_ID);
        }
        return "";
    }

    public static void setRestaurantId(ItemStack itemStack, String id) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(TAG_RESTAURANT_ID, id);
        MaidRestaurant.LOGGER.debug("Set RestaurantID:" + id);
    }

    public static ZoneDefinitionHandler getInventory(ItemStack stack) {
        ZoneDefinitionHandler handler = new ZoneDefinitionHandler(12);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_INVENTORY)) {
            handler.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        }
        return handler;
    }

    public static void setInventory(ItemStack stack, ItemStackHandler handler) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_INVENTORY, handler.serializeNBT());
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChefLicenseContainer(id, inv, player.getMainHandItem());
    }
}
