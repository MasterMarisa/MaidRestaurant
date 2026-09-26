package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.BellMenuItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record SendBellOrdersMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SendBellOrdersMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SendBellOrdersMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SendBellOrdersMessage(restaurantId, orders);
    }

    public static void handle(SendBellOrdersMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemInHand = player.getMainHandItem();
                String restaurantId = BellMenuItem.getRestaurantId(itemInHand);
                if (itemInHand.is(ModItems.BELL_MENU.get()) && restaurantId.equals(message.restaurantId)) {
                    BellMenuItem.setOrderEntries(itemInHand, message.orders());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
