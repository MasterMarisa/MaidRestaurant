package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ChefLicenseItem extends Item {
    public ChefLicenseItem(Properties properties) {
        super(properties);
    }

    public static String getRestaurantId(ItemStack itemStack) {
        return "";
    }

    public static boolean acceptRequests(ItemStack itemStack) {
        return false;
    }

    @Nullable
    public static AbstractZone getWorkZone(ItemStack itemStack) {
        return null;
    }

    @Nullable
    public static AbstractZone getStorageZone(ItemStack itemStack) {
        return null;
    }
}
