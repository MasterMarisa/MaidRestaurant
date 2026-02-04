package com.mastermarisa.maid_restaurant.network;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record CookOrderPayload(String[] recipeIDs, String[] recipeTypes, int[] counts, long[] tables) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CookOrderPayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("cook_order"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, long[]> LONG_ARRAY_STREAM_CODEC = StreamCodec.of(
            (buf, array) -> {
                buf.writeVarInt(array.length);
                for (long l : array) {
                    buf.writeLong(l);
                }
            },
            buf -> {
                int length = buf.readVarInt();
                long[] array = new long[length];
                for (int i = 0; i < length; i++) {
                    array[i] = buf.readLong();
                }
                return array;
            }
    );

    public static final StreamCodec<FriendlyByteBuf, int[]> INT_ARRAY_STREAM_CODEC = StreamCodec.of(
            (buf, array) -> {
                buf.writeVarInt(array.length);
                for (int i : array) {
                    buf.writeInt(i);
                }
            },
            buf -> {
                int length = buf.readVarInt();
                int[] array = new int[length];
                for (int i = 0; i < length; i++) {
                    array[i] = buf.readInt();
                }
                return array;
            }
    );

    public static final StreamCodec<FriendlyByteBuf, String[]> STRING_ARRAY_STREAM_CODEC = StreamCodec.of(
            (buf, array) -> {
                buf.writeVarInt(array.length);
                for (String s : array) {
                    buf.writeUtf(s);
                }
            },
            buf -> {
                int length = buf.readVarInt();
                String[] array = new String[length];
                for (int i = 0; i < length; i++) {
                    array[i] = buf.readUtf();
                }
                return array;
            }
    );

    public static final StreamCodec<FriendlyByteBuf, CookOrderPayload> STREAM_CODEC =
            StreamCodec.composite(
                    STRING_ARRAY_STREAM_CODEC,
                    CookOrderPayload::recipeIDs,
                    STRING_ARRAY_STREAM_CODEC,
                    CookOrderPayload::recipeTypes,
                    INT_ARRAY_STREAM_CODEC,
                    CookOrderPayload::counts,
                    LONG_ARRAY_STREAM_CODEC,
                    CookOrderPayload::tables,
                    CookOrderPayload::new
            );
}
