package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public interface ModEntities {
    DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister
            .create(Registries.MEMORY_MODULE_TYPE, MaidRestaurant.MOD_ID);

    Supplier<MemoryModuleType<PositionTracker>> TARGET_POS = MEMORY_MODULE_TYPES
            .register("target_pos", () -> new MemoryModuleType<>(Optional.empty()));
    Supplier<MemoryModuleType<Integer>> TARGET_TYPE = MEMORY_MODULE_TYPES
            .register("target_type", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
}
