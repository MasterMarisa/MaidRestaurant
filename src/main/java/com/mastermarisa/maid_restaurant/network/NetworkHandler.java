package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.network.message.RestaurantIdUpdateMessage;
import com.mastermarisa.maid_restaurant.network.message.SaveRecipeTreeMessage;
import com.mastermarisa.maid_restaurant.network.message.SaveUnboundMenuMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(MaidRestaurant.modLoc("network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int packetId = 0;

    public static void init() {
        CHANNEL.registerMessage(packetId++, RestaurantIdUpdateMessage.class, RestaurantIdUpdateMessage::encode, RestaurantIdUpdateMessage::decode, RestaurantIdUpdateMessage::handle);
        CHANNEL.registerMessage(packetId++, SaveRecipeTreeMessage.class, SaveRecipeTreeMessage::encode, SaveRecipeTreeMessage::decode, SaveRecipeTreeMessage::handle);
        CHANNEL.registerMessage(packetId++, SaveUnboundMenuMessage.class, SaveUnboundMenuMessage::encode, SaveUnboundMenuMessage::decode, SaveUnboundMenuMessage::handle);
    }

    public static void sendToClientPlayer(Object message, Player player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), message);
    }

    public static void sendToTrackingEntity(Object message, Entity centerEntity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> centerEntity), message);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    @SubscribeEvent
    public static void onSetupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
    }
}
