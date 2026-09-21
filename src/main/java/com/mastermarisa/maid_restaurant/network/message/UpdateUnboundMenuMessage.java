package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
                    CompoundTag tag = itemInHand.getOrCreateTag();
                    int[] indexList = tag.getIntArray("index_list");
                    int[] newList = new int[indexList.length - 1];
                    int j = 0;
                    for (int i : indexList) {
                        if (i != message.index) {
                            newList[j] = i;
                            j++;
                        }
                    }
                    ListTag entryList = tag.getList("entry_list", Tag.TAG_COMPOUND);
                    entryList.remove(message.index);
                    tag.putIntArray("index_list", newList);
                    tag.put("entry_list", entryList);
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
                    CompoundTag tag = itemInHand.getOrCreateTag();
                    int[] indexList = tag.getIntArray("index_list");
                    int[] newList = new int[indexList.length + 1];
                    newList[0] = message.index;
                    System.arraycopy(indexList, 0, newList, 1, indexList.length);
                    ListTag entryList = tag.getList("entry_list", Tag.TAG_COMPOUND);
                    entryList.add(message.tag);
                    tag.putIntArray("index_list", newList);
                    tag.put("entry_list", entryList);
                }
            });
            context.get().setPacketHandled(true);
        }
    }
}
