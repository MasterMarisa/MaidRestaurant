package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SendOrderPayload(String[] IDs, String[] types, int[] counts, long[] targets, byte[] attributes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendOrderPayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("send_order"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendOrderPayload> STREAM_CODEC =
            StreamCodec.composite(
                    NetworkHandler.STRING_ARRAY_STREAM_CODEC,
                    SendOrderPayload::IDs,
                    NetworkHandler.STRING_ARRAY_STREAM_CODEC,
                    SendOrderPayload::types,
                    NetworkHandler.INT_ARRAY_STREAM_CODEC,
                    SendOrderPayload::counts,
                    NetworkHandler.LONG_ARRAY_STREAM_CODEC,
                    SendOrderPayload::targets,
                    ByteBufCodecs.BYTE_ARRAY,
                    SendOrderPayload::attributes,
                    SendOrderPayload::new
            );
}
