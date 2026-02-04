package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record CancelRequestPayload(int actionCode, String uuid, int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CancelRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("cancel_request"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, CancelRequestPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    CancelRequestPayload::actionCode,
                    ByteBufCodecs.STRING_UTF8,
                    CancelRequestPayload::uuid,
                    ByteBufCodecs.INT,
                    CancelRequestPayload::index,
                    CancelRequestPayload::new
            );
}
