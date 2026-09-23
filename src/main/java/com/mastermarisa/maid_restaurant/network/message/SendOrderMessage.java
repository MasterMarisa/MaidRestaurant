package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.schedule.CookingRequestBus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record SendOrderMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SendOrderMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SendOrderMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SendOrderMessage(restaurantId, orders);
    }

    public static void handle(SendOrderMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                for (OrderEntry entry : message.orders) {
                    CookingRequest request = new CookingRequest(entry.getEntry().getRoot());
                    request.root.applyCount(player.level(), entry.getCount());
                    CookingRequestBus.getInstance((ServerLevel) player.level()).enqueue(message.restaurantId, request);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
