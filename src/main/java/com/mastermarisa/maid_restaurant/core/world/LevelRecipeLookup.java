package com.mastermarisa.maid_restaurant.core.world;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class LevelRecipeLookup implements IRecipeLookup {
    private final RecipeManager recipeManager;
    private final RegistryAccess registries;

    private LevelRecipeLookup(RecipeManager recipeManager, RegistryAccess registries) {
        this.recipeManager = recipeManager;
        this.registries = registries;
    }

    /**
     * 从任意 {@link Level} 构建视图
     */
    public static LevelRecipeLookup of(Level level) {
        return new LevelRecipeLookup(level.getRecipeManager(), level.registryAccess());
    }

    /**
     * 直接由 {@link RecipeManager} 与 {@link RegistryAccess} 构建视图
     */
    public static LevelRecipeLookup of(RecipeManager recipeManager, RegistryAccess registries) {
        return new LevelRecipeLookup(recipeManager, registries);
    }

    @Override
    @Nullable
    public Recipe<?> byKey(ResourceLocation id) {
        return recipeManager.byKey(id).orElse(null);
    }

    @Override
    public RegistryAccess registries() {
        return registries;
    }
}
