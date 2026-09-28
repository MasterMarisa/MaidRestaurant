package com.mastermarisa.maid_restaurant.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTaskEnableEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class MaidTracker {
    @SubscribeEvent
    public static void onMaidLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel)) {
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
