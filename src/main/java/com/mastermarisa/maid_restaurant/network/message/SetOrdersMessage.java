package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record SetOrdersMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SetOrdersMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SetOrdersMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SetOrdersMessage(restaurantId, orders);
    }

    public static void handle(SetOrdersMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemInHand = player.getMainHandItem();
                String restaurantId = RestaurantMenuItem.getRestaurantId(itemInHand);
                if (itemInHand.is(ModItems.RESTAURANT_MENU.get()) && restaurantId.equals(message.restaurantId)) {
                    RestaurantMenuItem.setOrderEntries(itemInHand, message.orders());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
