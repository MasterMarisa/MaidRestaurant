package com.mastermarisa.maid_restaurant.core.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ChefScheduler {
    /**
     * 获取女仆装备的厨师执照
     * @param maid 女仆
     * @return 执照物品,若无则返还 ItemStack.EMPTY
     */
    public static ItemStack getChefLicense(EntityMaid maid) {
        BaubleItemHandler handler = maid.getMaidBauble();
        int index = handler.getBaubleSlot(BaubleManager.getBauble(ModItems.CHEF_LICENSE));
        if (index == -1) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(index);
    }

    /**
     * 获取女仆的工作区
     * @param maid 女仆
     * @return 工作区
     */
    @Nullable
    public static AbstractZone getWorkZone(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        return ChefLicenseItem.getWorkZone(license);
    }

    /**
     * 获取女仆的库存区
     * @param maid 女仆
     * @return 库存区
     */
    @Nullable
    public static AbstractZone getStorageZone(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        return ChefLicenseItem.getStorageZone(license);
    }

    /**
     * 获取女仆已认领的委托,若不存在则尝试从总线认领
     * @param level 所在Level
     * @param maid 女仆
     * @return 认领的委托
     */
    @Nullable
    public static CookingRequest getOrClaimRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String id = ChefLicenseItem.getRestaurantId(license);
        CookingRequestBus bus = CookingRequestBus.get(level);
        CookingRequest request = bus.getClaimed(id, maid);
        if (request != null) {
            return request;
        }
        if (ChefLicenseItem.acceptRequests(license)) {
            return bus.claim(id, maid, level.getGameTime());
        }
        return null;
    }

    /**
     * 释放女仆已认领的委托,并重新认领另一个委托
     * @param level 所在Level
     * @param maid 女仆
     * @return 认领的委托
     */
    @Nullable
    public static CookingRequest reclaimRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String id = ChefLicenseItem.getRestaurantId(license);
        CookingRequestBus bus = CookingRequestBus.get(level);
        return bus.reclaim(id, maid, level.getGameTime());
    }
}
