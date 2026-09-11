package com.mastermarisa.maid_restaurant.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.Debug;
import com.mastermarisa.maid_restaurant.utils.EncodeUtils;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
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
        Debug.Log(" received_send_order_packet, length:" + IDs.length);
        for (int i = 0;i < IDs.length;i++) {
            List<CookRequest> mapped = tryMap(context.player().level(), new CookRequest(
                    ResourceLocation.parse(IDs[i]),
                    CookTasks.getType(types[i]),
                    counts[i],
                    counts[i],
                    payload.targets(),
                    payload.attributes()
            ));

            for (var request : mapped) {
                RequestManager.post((ServerLevel) context.player().level(), request, CookRequest.TYPE);
            }
        }
    }

    private static List<CookRequest> tryMap(Level level, CookRequest request) {
        List<CookRequest> mapped = new ArrayList<>();

        if (request.type.equals(ModRecipes.POT_RECIPE)) {
            PotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.POT_RECIPE, request.id).value();
            ItemStack result = recipe.result();
            int count = result.getCount() * request.requested;
            if (result.is(ModItems.MEAT_PIE.get()) && result.getCount() != 9) {
                if (count > 9) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/stuffed_dough_food_to_meat_pie_9"),
                        ModRecipes.POT_RECIPE,
                        count / 9,
                        count / 9,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 9 != 0) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/stuffed_dough_food_to_meat_pie_" + count % 9),
                        ModRecipes.POT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            } else if (result.is(ModItems.FRIED_EGG.get()) && result.getCount() != 9) {
                if (count > 9) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/egg_to_fried_egg_9"),
                        ModRecipes.POT_RECIPE,
                        count / 9,
                        count / 9,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 9 != 0) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/egg_to_fried_egg_" + count % 9),
                        ModRecipes.POT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            } else if (result.is(EncodeUtils.decode("kaleidoscope_cookery:sweet_and_sour_ender_pearls")) && result.getCount() != 3) {
                if (count > 3) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/sweet_and_sour_ender_pearls_3"),
                        ModRecipes.POT_RECIPE,
                        count / 3,
                        count / 3,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 3 != 0) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/sweet_and_sour_ender_pearls_" + count % 3),
                        ModRecipes.POT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            } else if (result.is(ModItems.EGG_FRIED_RICE.get()) && result.getCount() != 3) {
                if (count > 3) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/egg_fried_rice_3"),
                        ModRecipes.POT_RECIPE,
                        count / 3,
                        count / 3,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 3 == 2) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:pot/egg_fried_rice_2"),
                        ModRecipes.POT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            }
        } else if (request.type.equals(ModRecipes.STOCKPOT_RECIPE)) {
            StockpotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE, request.id).value();
            ItemStack result = recipe.result();
            int count = result.getCount() * request.requested;
            if (result.is(ModItems.DUMPLING.get()) && result.getCount() != 9) {
                if (count > 9) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:stockpot/dumpling_count_9"),
                        ModRecipes.STOCKPOT_RECIPE,
                        count / 9,
                        count / 9,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 9 != 0) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:stockpot/dumpling_count_" + count % 9),
                        ModRecipes.STOCKPOT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            } else if(result.is(EncodeUtils.decode("kaleidoscope_cookery:shengjian_mantou")) && result.getCount() != 2) {
                if (count > 2) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:stockpot/shengjian_mantou_count_2"),
                        ModRecipes.STOCKPOT_RECIPE,
                        count / 2,
                        count / 2,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());

                if (count % 2 != 0) mapped.add(new CookRequest(
                        ResourceLocation.parse("kaleidoscope_cookery:stockpot/shengjian_mantou_count_1"),
                        ModRecipes.STOCKPOT_RECIPE,
                        1,
                        1,
                        request.targets,
                        request.attributes.getAttributes()
                ).copy());
            }
        }

        if (mapped.isEmpty()) mapped.add(request);
        return mapped;
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
