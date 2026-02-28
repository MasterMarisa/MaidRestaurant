package com.mastermarisa.maid_restaurant.event;

import com.mastermarisa.maid_restaurant.init.ModTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class EnterServerEvent {
    @SubscribeEvent
    public static void onAttachCapabilityEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            ModTrigger.GIVE_PATCHOULI_BOOK_CONFIG.get().trigger(serverPlayer);
        }
    }
}
