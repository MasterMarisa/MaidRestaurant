package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChangeHandlerAcceptValuePayload(int t, UUID uuid, boolean value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChangeHandlerAcceptValuePayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("change_accept_value"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, ChangeHandlerAcceptValuePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ChangeHandlerAcceptValuePayload::t,
                    NetworkHandler.UUID_STREAM_CODEC,
                    ChangeHandlerAcceptValuePayload::uuid,
                    ByteBufCodecs.BOOL,
                    ChangeHandlerAcceptValuePayload::value,
                    ChangeHandlerAcceptValuePayload::new
            );
}
