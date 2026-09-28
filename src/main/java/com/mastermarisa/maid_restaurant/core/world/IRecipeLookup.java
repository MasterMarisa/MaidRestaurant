package com.mastermarisa.maid_restaurant.core.world;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;

public interface IRecipeLookup {
    @Nullable
    Recipe<?> byKey(ResourceLocation id);

    RegistryAccess registries();
}
