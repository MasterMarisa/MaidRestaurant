package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.entity.SitEntity;
import com.mastermarisa.maid_restaurant.entity.attachment.BlockSelection;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public interface InitEntities {
    DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MaidRestaurant.MOD_ID);

    DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MaidRestaurant.MOD_ID);

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MaidRestaurant.MOD_ID);

    DeferredHolder<MemoryModuleType<?>,MemoryModuleType<PositionTracker>> TARGET_POS = MEMORY_MODULE_TYPES
            .register("target_pos", () -> new MemoryModuleType<>(Optional.empty()));

    DeferredHolder<MemoryModuleType<?>,MemoryModuleType<Integer>> TARGET_TYPE = MEMORY_MODULE_TYPES
            .register("target_type", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    DeferredHolder<MemoryModuleType<?>,MemoryModuleType<PositionTracker>> CHAIR_POS = MEMORY_MODULE_TYPES
            .register("chair_pos", () -> new MemoryModuleType<>(Optional.empty()));

    DeferredHolder<EntityType<?>,EntityType<SitEntity>> SIT_ENTITY = ENTITY_TYPES.register(
            "sit_entity", () -> SitEntity.TYPE
    );

    Supplier<AttachmentType<CookRequestQueue>> COOK_REQUEST_QUEUE = ATTACHMENT_TYPES.register("cook_request_queue",() -> CookRequestQueue.TYPE);

    Supplier<AttachmentType<ServeRequestQueue>> SERVE_REQUEST_QUEUE = ATTACHMENT_TYPES.register("serve_request_queue",() -> ServeRequestQueue.TYPE);

    Supplier<AttachmentType<BlockSelection>> BLOCK_SELECTION = ATTACHMENT_TYPES.register("block_selection",() -> BlockSelection.TYPE);

    static void register(IEventBus mod) {
        MEMORY_MODULE_TYPES.register(mod);
        ENTITY_TYPES.register(mod);
        ATTACHMENT_TYPES.register(mod);
    }
}
