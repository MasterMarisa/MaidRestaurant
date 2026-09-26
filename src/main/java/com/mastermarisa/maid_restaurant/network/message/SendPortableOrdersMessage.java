package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.PortableMenuItem;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequestBus;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SendPortableOrdersMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SendPortableOrdersMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SendPortableOrdersMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SendPortableOrdersMessage(restaurantId, orders);
    }

    public static void handle(SendPortableOrdersMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.is(ModItems.PORTABLE_MENU.get())) {
                    ServerLevel level = player.serverLevel();
                    List<ServeRequest.Target> targets = PortableMenuItem.getTargets(itemStack);
                    for (OrderEntry entry : message.orders()) {
                        RecipeNode root = entry.getEntry().getRoot().copy();
                        CookingRequest request = new CookingRequest(root);
                        request.root.applyCount(level, entry.getCount());
                        if (!targets.isEmpty()) {
                            request.boundRequest = new ServeRequest(root.getIngredient(), entry.getCount());;
                            request.boundRequest.targets = new ArrayList<>(targets);
                        }
                        CookingRequestBus.getInstance(level).enqueue(message.restaurantId(), request);
                    }
                    PortableMenuItem.removeTargets(itemStack);
                    MutableComponent component = Component.literal("下单成功!").withStyle(ChatFormatting.GREEN);
                    player.connection.send(new ClientboundSetActionBarTextPacket(component));
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
