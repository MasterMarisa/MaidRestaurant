package com.mastermarisa.maid_restaurant.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                SendOrderPayload.TYPE,
                SendOrderPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        handleSendOrdersOnServer(payload,context);
                    });
                }
        );

        registrar.playToServer(
                ModifyAttributePayload.TYPE,
                ModifyAttributePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        handleModifyAttributesOnServer(payload,context);
                    });
                }
        );

        registrar.playToServer(
                CancelRequestPayload.TYPE,
                CancelRequestPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        handleCancelRequestOnServer(payload,context);
                    });
                }
        );

        registrar.playToServer(
                ChangeHandlerAcceptValuePayload.TYPE,
                ChangeHandlerAcceptValuePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        handleChangeHandlerAcceptValueOnServer(payload,context);
                    });
                }
        );

        registrar.playToClient(
                OpenScreenPayload.TYPE,
                OpenScreenPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        OpenScreenPayload.handle(payload,context);
                    });
                }
        );
    }

    private static void handleSendOrdersOnServer(SendOrderPayload payload, IPayloadContext context) {
        String[] IDs = payload.IDs();
        String[] types = payload.types();
        int[] counts = payload.counts();
        for (int i = 0;i < IDs.length;i++) {
            RequestManager.post((ServerLevel) context.player().level(),new CookRequest(
                    ResourceLocation.parse(IDs[i]),
                    CookTasks.getType(types[i]),
                    counts[i],
                    counts[i],
                    payload.targets(),
                    payload.attributes()
            ), CookRequest.TYPE);
        }
    }

    private static void handleModifyAttributesOnServer(ModifyAttributePayload payload, IPayloadContext context) {
        ServerLevel level = (ServerLevel) context.player().level();
        if (level.getEntity(payload.uuid()) instanceof EntityMaid maid) {
            CookRequestHandler handler = maid.getData(CookRequestHandler.TYPE);
            if (handler.size() > payload.index()) {
                Objects.requireNonNull(handler.getAt(payload.index())).attributes.setAttributes(payload.attributes());
            }
        }
    }

    private static void handleCancelRequestOnServer(CancelRequestPayload payload, IPayloadContext context) {
        ServerLevel level = (ServerLevel) context.player().level();
        if (level.getEntity(payload.uuid()) instanceof EntityMaid maid) {
            switch (payload.actionCode()) {
                case 0 -> {
                    CookRequestHandler handler = maid.getData(CookRequestHandler.TYPE);
                    handler.removeAt(payload.index());
                    maid.removeData(CookRequestHandler.TYPE);
                    maid.setData(CookRequestHandler.TYPE,handler);
                }
                case 1 -> {
                    ServeRequestHandler handler = maid.getData(ServeRequestHandler.TYPE);
                    handler.removeAt(payload.index());
                    maid.removeData(ServeRequestHandler.TYPE);
                    maid.setData(ServeRequestHandler.TYPE,handler);
                }
            }
        }
    }

    private static void handleChangeHandlerAcceptValueOnServer(ChangeHandlerAcceptValuePayload payload, IPayloadContext context) {
        ServerLevel level = (ServerLevel) context.player().level();
        if (level.getEntity(payload.uuid()) instanceof EntityMaid maid) {
            switch (payload.t()) {
                case 0 -> {
                    CookRequestHandler handler = maid.getData(CookRequestHandler.TYPE);
                    handler.accept = payload.value();
                    maid.removeData(CookRequestHandler.TYPE);
                    maid.setData(CookRequestHandler.TYPE,handler);
                }
                case 1 -> {
                    ServeRequestHandler handler = maid.getData(ServeRequestHandler.TYPE);
                    handler.accept = payload.value();
                    maid.removeData(ServeRequestHandler.TYPE);
                    maid.setData(ServeRequestHandler.TYPE,handler);
                }
            }
        }
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

    public static final StreamCodec<FriendlyByteBuf, UUID> UUID_STREAM_CODEC = StreamCodec.of(
            (buf, uuid) -> buf.writeUUID(uuid),
            buf -> buf.readUUID()
    );
}
