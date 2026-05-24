package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SaveRecipeTreeMessage(CompoundTag root) {
    public static void encode(SaveRecipeTreeMessage packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.root);
    }

    public static SaveRecipeTreeMessage decode(FriendlyByteBuf buf) {
        return new SaveRecipeTreeMessage(buf.readNbt());
    }

    public static void handle(SaveRecipeTreeMessage packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.getMainHandItem().is(ModItems.COOKING_GUIDE.get())) {
                CookingGuideItem.setRecipeRoot(player.getMainHandItem(), packet.root);
            }
        });
        context.get().setPacketHandled(true);
    }
}
