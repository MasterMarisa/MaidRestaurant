package com.mastermarisa.maid_restaurant.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequest;
import com.mastermarisa.maid_restaurant.core.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class EntityJoinLevel {
    @SubscribeEvent
    public static void onMaidJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        if (maid.getTask() instanceof TaskChef) {
            ItemStack itemStack = ChefScheduler.getChefLicense(maid);
            if (!itemStack.isEmpty()) {
                String id = ChefLicenseItem.getRestaurantId(itemStack);
                CookingRequest request = RequestBus.getInstance(level, CookingRequest.class).getClaimed(id, maid);
                if (request != null) {
                    request.root.verifyAndUpdateState(level, maid);
                }
            }
        }
    }
}
