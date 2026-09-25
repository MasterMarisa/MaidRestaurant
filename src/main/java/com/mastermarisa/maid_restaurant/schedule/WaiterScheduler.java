package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.WaiterLicenseItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WaiterScheduler {
    /**
     * 获取女仆装备的厨师执照
     * @param maid 女仆
     * @return 执照物品,若无则返还 ItemStack.EMPTY
     */
    public static ItemStack getWaiterLicense(EntityMaid maid) {
        BaubleItemHandler handler = maid.getMaidBauble();
        int index = handler.getBaubleSlot(BaubleManager.getBauble(ModItems.WAITER_LICENSE));
        if (index == -1) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(index);
    }

    @Nullable
    public static String getRestaurantId(EntityMaid maid) {
        ItemStack license = getWaiterLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String restaurantId = WaiterLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return null;
        }
        return restaurantId;
    }

    @Nullable
    public static ServeRequest getOrClaimRequest(ServerLevel level, EntityMaid maid) {
        String restaurantId = getRestaurantId(maid);
        if (restaurantId == null) {
            return null;
        }
        ServeRequestBus bus = ServeRequestBus.getInstance(level);
        ServeRequest request = bus.getClaimed(restaurantId, maid);
        if (request == null) {
            request = bus.claim(restaurantId, maid);
        }
        return request;
    }

    public static void submitRequest(ServerLevel level, EntityMaid maid) {
        String restaurantId = getRestaurantId(maid);
        if (restaurantId == null) {
            return;
        }
        ServeRequestBus bus = ServeRequestBus.getInstance(level);
        bus.submit(restaurantId, maid);
    }
}
