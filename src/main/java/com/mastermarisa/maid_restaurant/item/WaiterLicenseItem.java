package com.mastermarisa.maid_restaurant.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WaiterLicenseItem extends Item {
    private static final String TAG_RESTAURANT_ID = "restaurant_id";

    public WaiterLicenseItem(Properties properties) { super(properties); }

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
    }
}
