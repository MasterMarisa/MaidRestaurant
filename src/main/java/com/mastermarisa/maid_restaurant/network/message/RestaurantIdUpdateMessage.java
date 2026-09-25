package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.item.WaiterLicenseItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RestaurantIdUpdateMessage(String id) {
    public static void encode(RestaurantIdUpdateMessage packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.id);
    }

    public static RestaurantIdUpdateMessage decode(FriendlyByteBuf buf) {
        return new RestaurantIdUpdateMessage(buf.readUtf());
    }

    public static void handle(RestaurantIdUpdateMessage packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.is(ModItems.CHEF_LICENSE.get())) {
                    ChefLicenseItem.setRestaurantId(itemStack, packet.id);
                }
                if (itemStack.is(ModItems.WAITER_LICENSE.get())) {
                    WaiterLicenseItem.setRestaurantId(itemStack, packet.id);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
