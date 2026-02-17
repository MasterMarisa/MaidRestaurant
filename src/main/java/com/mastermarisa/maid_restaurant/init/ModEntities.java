package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.request.world.WorldCookRequestHandler;
import com.mastermarisa.maid_restaurant.request.world.WorldServeRequestHandler;
import com.mastermarisa.maid_restaurant.utils.component.BlockSelection;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class ModEntities {
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.
            create(Registries.MEMORY_MODULE_TYPE, MaidRestaurant.MOD_ID);

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.
            create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MaidRestaurant.MOD_ID);

    public static final DeferredHolder<MemoryModuleType<?>,MemoryModuleType<PositionTracker>> TARGET_POS = MEMORY_MODULE_TYPES
            .register("target_pos", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>,MemoryModuleType<Integer>> TARGET_TYPE = MEMORY_MODULE_TYPES
            .register("target_type", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final DeferredHolder<MemoryModuleType<?>,MemoryModuleType<PositionTracker>> CACHED_WORK_BLOCK = MEMORY_MODULE_TYPES
            .register("cached_work_block", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>,MemoryModuleType<PositionTracker>> CHAIR_POS = MEMORY_MODULE_TYPES
            .register("chair_pos", () -> new MemoryModuleType<>(Optional.empty()));

    private static final Supplier<AttachmentType<CookRequestHandler>> COOK_REQUEST_HANDLER = ATTACHMENT_TYPES.
            register("cook_request_handler",() -> CookRequestHandler.TYPE);

    private static final Supplier<AttachmentType<ServeRequestHandler>> SERVE_REQUEST_HANDLER = ATTACHMENT_TYPES.
            register("serve_request_handler",() -> ServeRequestHandler.TYPE);

    private static final Supplier<AttachmentType<WorldCookRequestHandler>> WORLD_COOK_REQUEST_HANDLER = ATTACHMENT_TYPES.
            register("world_cook_request_handler",() -> WorldCookRequestHandler.TYPE);

    private static final Supplier<AttachmentType<WorldServeRequestHandler>> WORLD_SERVE_REQUEST_HANDLER = ATTACHMENT_TYPES.
            register("world_serve_request_handler",() -> WorldServeRequestHandler.TYPE);

    private static final Supplier<AttachmentType<BlockSelection>> BLOCK_SELECTION = ATTACHMENT_TYPES.
            register("block_selection",() -> BlockSelection.TYPE);

    public static void register(IEventBus mod) {
        MEMORY_MODULE_TYPES.register(mod);
        ATTACHMENT_TYPES.register(mod);
    }
}
