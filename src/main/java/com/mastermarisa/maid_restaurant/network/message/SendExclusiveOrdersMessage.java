package com.mastermarisa.maid_restaurant.network.message;

import com.mastermarisa.maid_restaurant.core.request.CookingRequestBus;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.core.request.CookingRequest;
import com.mastermarisa.maid_restaurant.core.request.ServingRequest;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.ExclusiveMenuItem;
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

public record SendExclusiveOrdersMessage(String restaurantId, List<OrderEntry> orders) {
    public static void encode(SendExclusiveOrdersMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.restaurantId);
        buf.writeCollection(message.orders.stream().map(OrderEntry::serializeNBT).toList(), FriendlyByteBuf::writeNbt);
    }

    public static SendExclusiveOrdersMessage decode(FriendlyByteBuf buf) {
        String restaurantId = buf.readUtf();
        List<OrderEntry> orders = buf.readList(FriendlyByteBuf::readNbt).stream().map(OrderEntry::fromNBT).toList();
        return new SendExclusiveOrdersMessage(restaurantId, orders);
    }

    public static void handle(SendExclusiveOrdersMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.is(ModItems.EXCLUSIVE_MENU.get())) {
                    ServerLevel level = player.serverLevel();
                    List<ServingRequest.Target> targets = ExclusiveMenuItem.getTargets(itemStack);
                    for (OrderEntry entry : message.orders()) {
                        RecipeNode root = entry.getEntry().getRoot().copy();
                        CookingRequest request = new CookingRequest(root);
                        request.root.applyCount(level, entry.getCount());
                        if (!targets.isEmpty()) {
                            request.boundRequest = new ServingRequest(root.getIngredient(), entry.getCount());;
                            request.boundRequest.targets = new ArrayList<>(targets);
                        }
                        CookingRequestBus.getInstance(level).enqueue(message.restaurantId(), request);
                    }
                    MutableComponent component = Component.literal("下单成功!").withStyle(ChatFormatting.GREEN);
                    player.connection.send(new ClientboundSetActionBarTextPacket(component));
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
