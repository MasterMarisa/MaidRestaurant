package com.mastermarisa.maid_restaurant.compat.jei;

import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.crafting.recipe.MenuTransformingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import java.util.List;

public class MenuTransformingRecipeMaker {
    public static List<CraftingRecipe> createRecipes() {
        List<CraftingRecipe> recipes = Lists.newArrayList();
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                    .filter(MenuTransformingRecipe.class::isInstance)
                    .map(MenuTransformingRecipe.class::cast)
                    .map(recipe -> new ShapelessRecipe(
                            recipe.getId(),
                            recipe.getGroup(),
                            recipe.category(),
                            recipe.getResultItem(level.registryAccess()),
                            recipe.getIngredients()
                    ))
                    .forEach(recipes::add);
        }
        return recipes;
    }

    private MenuTransformingRecipeMaker() {
    }
}
