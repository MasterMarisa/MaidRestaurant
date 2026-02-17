package com.mastermarisa.maid_restaurant.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.client.gui.screen.serve_request.ServeRequestScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenScreenPayload(int actionCode, int id) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(MaidRestaurant.resourceLocation("open_screen"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, OpenScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    OpenScreenPayload::actionCode,
                    ByteBufCodecs.INT,
                    OpenScreenPayload::id,
                    OpenScreenPayload::new
            );

    public static void handle(OpenScreenPayload payload, IPayloadContext context) {
        Level level = context.player().level();
        if (level.getEntity(payload.id) instanceof EntityMaid maid) {
            switch (payload.actionCode) {
                case 0 -> CookRequestScreen.open(maid);
                case 1 -> ServeRequestScreen.open(maid);
            }
        }
    }
}
