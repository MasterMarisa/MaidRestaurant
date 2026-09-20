package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SaveUnboundMenuMessage(CompoundTag tag) {
    public static void encode(SaveUnboundMenuMessage message, FriendlyByteBuf buf) {
        buf.writeNbt(message.tag);
    }

    public static SaveUnboundMenuMessage decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SaveUnboundMenuMessage(tag);
    }

    public static void handle(SaveUnboundMenuMessage packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.getMainHandItem().is(ModItems.UNBOUND_MENU.get())) {
                ItemStack itemInHand = player.getMainHandItem();
                UnboundMenuItem.setMenuEntries(itemInHand, packet.tag());
            }
        });
        context.get().setPacketHandled(true);
    }
}
