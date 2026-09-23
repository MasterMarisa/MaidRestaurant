package com.mastermarisa.maid_restaurant.datagen.recipe;

import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ShapedRecipeProvider extends ModRecipeProvider {
    public ShapedRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CHEF_LICENSE.get())
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.WHITE_WOOL)
                .define('B', ModItems.COOKING_GUIDE.get())
                .unlockedBy("has_cooking_guide", has(ModItems.COOKING_GUIDE.get()))
                .save(consumer);
    }
}