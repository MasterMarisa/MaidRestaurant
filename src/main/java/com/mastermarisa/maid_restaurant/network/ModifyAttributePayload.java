package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ModifyAttributePayload(int actionCode, UUID uuid, int index, byte[] attributes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ModifyAttributePayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("modify_attributes"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, ModifyAttributePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ModifyAttributePayload::actionCode,
                    NetworkHandler.UUID_STREAM_CODEC,
                    ModifyAttributePayload::uuid,
                    ByteBufCodecs.INT,
                    ModifyAttributePayload::index,
                    ByteBufCodecs.BYTE_ARRAY,
                    ModifyAttributePayload::attributes,
                    ModifyAttributePayload::new
            );
}
