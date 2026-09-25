package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record BindMenuMessage(String restaurantId) {
    public static void encode(BindMenuMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
    }

    public static BindMenuMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        return new BindMenuMessage(restaurantId);
    }

    public static void handle(BindMenuMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.getMainHandItem().is(ModItems.UNBOUND_MENU.get())) {
                ItemStack itemInHand = player.getMainHandItem();
                player.setItemInHand(InteractionHand.MAIN_HAND, ModItems.RESTAURANT_MENU.get().getDefaultInstance());
                Map<Integer, MenuEntry> map = UnboundMenuItem.getEntries(itemInHand);
                UnboundMenuItem.setEntries(player.getMainHandItem(), map);
                RestaurantMenuItem.setRestaurantId(player.getMainHandItem(), message.restaurantId);
            }
        });
        context.get().setPacketHandled(true);
    }
}
