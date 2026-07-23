package com.mastermarisa.maid_restaurant.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTaskEnableEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.schedule.CookingRequestBus;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class MaidTracker {
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
                CookingRequest request = CookingRequestBus.getInstance(level).getClaimed(id, maid);
                if (request != null) {
                    request.root.verifyAndUpdateState(level, maid);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMaidLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p -> {
            BlockUsageUtil.remove(p.currentBlockPosition(), maid.getUUID());
        });
    }

    @SubscribeEvent
    public static void onMaidTaskEnable(MaidTaskEnableEvent event) {
        EntityMaid maid = event.getEntityMaid();
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p -> {
            BlockUsageUtil.remove(p.currentBlockPosition(), maid.getUUID());
        });
    }
}
