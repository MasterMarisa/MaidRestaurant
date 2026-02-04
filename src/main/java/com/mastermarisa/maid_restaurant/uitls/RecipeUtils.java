package com.mastermarisa.maid_restaurant.uitls;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeUtils {
    private static RecipeManager recipeManager;

    public static @Nullable RecipeHolder<? extends Recipe<?>> byKeyTyped(RecipeType<?> type, ResourceLocation name){
        return recipeManager.byKeyTyped(type,name);
    }

    public static void getAllRecipesFor(RecipeType<?> type) {

    }

    public static Optional<RecipeHolder<?>> byKey(ResourceLocation name){
        return recipeManager.byKey(name);
    }

    public static Optional<RecipeItem.RecipeRecord> getRecord(ItemStack itemStack) {
        if (RecipeItem.hasRecipe(itemStack)) return Optional.ofNullable(RecipeItem.getRecipe(itemStack));
        return Optional.empty();
    }

    public static void setRecipeManager(RecipeManager manager) { recipeManager = manager; }

    public static @Nullable RecipeManager getRecipeManager() { return recipeManager; }
}
