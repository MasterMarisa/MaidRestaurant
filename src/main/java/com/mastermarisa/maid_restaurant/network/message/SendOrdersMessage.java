package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.schedule.CookingRequestBus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record SendOrdersMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SendOrdersMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SendOrdersMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SendOrdersMessage(restaurantId, orders);
    }

    public static void handle(SendOrdersMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.is(ModItems.RESTAURANT_MENU.get())) {
                    ServerLevel level = player.serverLevel();
                    List<ServeRequest.Target> targets = RestaurantMenuItem.getTargets(itemStack);
                    for (OrderEntry entry : message.orders()) {
                        CookingRequest cookingRequest = new CookingRequest(entry.getEntry().getRoot());
                        cookingRequest.root.applyCount(level, entry.getCount());
                        if (!targets.isEmpty()) {
                            ServeRequest serveRequest = new ServeRequest(cookingRequest.root.getIngredient(), entry.getCount());
                            serveRequest.targets = targets;
                            cookingRequest.boundRequest = serveRequest;
                        }
                        CookingRequestBus.getInstance(level).enqueue(message.restaurantId(), cookingRequest);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
