package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RestaurantIdUpdateMessage(String id) {
    public static void encode(RestaurantIdUpdateMessage packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.id);
    }

    public static RestaurantIdUpdateMessage decode(FriendlyByteBuf buf) {
        return new RestaurantIdUpdateMessage(buf.readUtf());
    }

    public static void handle(RestaurantIdUpdateMessage packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getMainHandItem().is(ModItems.CHEF_LICENSE.get())) {
                ChefLicenseItem.setRestaurantId(player.getMainHandItem(), packet.id);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
