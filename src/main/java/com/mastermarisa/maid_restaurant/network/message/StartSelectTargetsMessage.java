package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record StartSelectTargetsMessage() {
    public static void encode(StartSelectTargetsMessage message, FriendlyByteBuf buf) {
    }

    public static StartSelectTargetsMessage decode(FriendlyByteBuf buf) {
        return new StartSelectTargetsMessage();
    }

    public static void handle(StartSelectTargetsMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemInHand = player.getMainHandItem();
                if (itemInHand.is(ModItems.RESTAURANT_MENU.get()) && !RestaurantMenuItem.isOrdering(itemInHand)) {
                    RestaurantMenuItem.setSelectingTargets(itemInHand, true);
                    RestaurantMenuItem.setTargets(itemInHand, List.of());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
