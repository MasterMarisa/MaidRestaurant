package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class UpdateUnboundMenuMessage {
    public record Remove(int index) {
        public static void encode(Remove message, FriendlyByteBuf buf) {
            buf.writeVarInt(message.index);
        }

        public static Remove decode(FriendlyByteBuf buf) {
            int index = buf.readVarInt();
            return new Remove(index);
        }

        public static void handle(Remove message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player != null && player.getMainHandItem().is(ModItems.UNBOUND_MENU.get())) {
                    ItemStack itemInHand = player.getMainHandItem();
                    Map<Integer, MenuEntry> map = UnboundMenuItem.getEntries(itemInHand);
                    map.remove(message.index);
                    UnboundMenuItem.setEntries(itemInHand, map);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    public record Update(int index, CompoundTag tag) {
        public static void encode(Update message, FriendlyByteBuf buf) {
            buf.writeVarInt(message.index);
            buf.writeNbt(message.tag);
        }

        public static Update decode(FriendlyByteBuf buf) {
            int index = buf.readVarInt();
            CompoundTag tag = buf.readNbt();
            return new Update(index, tag);
        }

        public static void handle(Update message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player != null && player.getMainHandItem().is(ModItems.UNBOUND_MENU.get())) {
                    ItemStack itemInHand = player.getMainHandItem();
                    Map<Integer, MenuEntry> map = UnboundMenuItem.getEntries(itemInHand);
                    map.put(message.index, MenuEntry.fromNBT(message.tag));
                    UnboundMenuItem.setEntries(itemInHand, map);
                }
            });
            context.get().setPacketHandled(true);
        }
    }
}
