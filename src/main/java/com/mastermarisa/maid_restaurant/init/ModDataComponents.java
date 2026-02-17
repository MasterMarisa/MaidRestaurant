package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MaidRestaurant.MOD_ID);

    public static final Supplier<DataComponentType<List<CookRequest>>> RECORDED_COOK_REQUESTS = DATA_COMPONENT_TYPES.register("recorded_cook_requests",
            () -> DataComponentType.<List<CookRequest>>builder().persistent(CookRequest.CODEC.listOf()).networkSynchronized(CookRequest.STREAM_CODEC.apply(ByteBufCodecs.list())).build()
    );

    public static void register(IEventBus mod) {
        DATA_COMPONENT_TYPES.register(mod);
    }
}
