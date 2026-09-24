package com.mastermarisa.maid_restaurant.datagen.recipe;

import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ShapelessRecipeProvider extends ModRecipeProvider {
    public ShapelessRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.COOKING_GUIDE.get(), 1)
                .requires(Items.PAPER)
                .requires(Items.CARROT)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.UNBOUND_MENU.get(), 1)
                .requires(Items.WRITABLE_BOOK)
                .requires(ModItems.COOKING_GUIDE.get(), 3)
                .unlockedBy("has_cooking_guide", has(ModItems.COOKING_GUIDE.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.CUBOID_ZONE_DEFINITION.get())
                .requires(Items.PAPER)
                .requires(Items.LIGHT_BLUE_DYE)
                .requires(Items.CHEST)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.POINTSET_ZONE_DEFINITION.get())
                .requires(Items.PAPER)
                .requires(Items.LIGHT_BLUE_DYE)
                .requires(ItemTags.WOODEN_BUTTONS)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);
    }
}
