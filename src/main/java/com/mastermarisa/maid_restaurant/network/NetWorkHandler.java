package com.mastermarisa.maid_restaurant.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

public class NetWorkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                CookOrderPayload.TYPE,
                CookOrderPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        handleCookOrdersOnServer(payload,context);
                    });
                }
        );

        registrar.playToServer(
                CancelRequestPayload.TYPE,
                CancelRequestPayload.STREAM_CODEC,
                ((payload, context) -> {
                    context.enqueueWork(() -> {
                        handleCancelRequestOnServer(payload,context);
                    });
                })
        );
    }

    private static void handleCookOrdersOnServer(CookOrderPayload payload, IPayloadContext context) {
        String[] IDs = payload.recipeIDs();
        String[] types = payload.recipeTypes();
        int[] counts = payload.counts();
        long[] tables = payload.tables();

        for (int i = 0;i < IDs.length;i++) {
            CookRequest request = new CookRequest(IDs[i],types[i],counts[i]);
            RequestManager.postCookRequest(request, BlockPosUtils.unpack(tables));
        }
    }

    private static void handleCancelRequestOnServer(CancelRequestPayload payload, IPayloadContext context) {
        Player player = context.player();
        ServerLevel level = (ServerLevel) player.level();

        switch (payload.actionCode()) {
            case 0:
                if (level.getEntity(UUID.fromString(payload.uuid())) instanceof EntityMaid maid) {
                    CookRequestQueue cookRequestQueue = maid.getData(CookRequestQueue.TYPE);
                    ServeRequestQueue serveRequestQueue = maid.getData(ServeRequestQueue.TYPE);
                    cookRequestQueue.removeAt(payload.index());
                    serveRequestQueue.removeAt(payload.index());
                    maid.setData(CookRequestQueue.TYPE,cookRequestQueue);
                    maid.setData(ServeRequestQueue.TYPE,serveRequestQueue);
                }
                break;
            case 1:
                if (level.getEntity(UUID.fromString(payload.uuid())) instanceof EntityMaid maid) {
                    ServeRequestQueue serveRequestQueue = maid.getData(ServeRequestQueue.TYPE);
                    serveRequestQueue.removeAt(payload.index());
                    maid.setData(ServeRequestQueue.TYPE,serveRequestQueue);
                }
                break;
        }
    }
}
