package com.mastermarisa.maid_restaurant.compat.jei;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.init.tag.TagMod;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

public class MenuTransformingRecipeMaker {
    public static List<CraftingRecipe> createRecipes() {
        CraftingRecipe menuUnbinding = new ShapelessRecipe(MaidRestaurant.modLoc("menu_unbinding"), "", CraftingBookCategory.MISC, ModItems.UNBOUND_MENU.get().getDefaultInstance(), NonNullList.of(Ingredient.EMPTY, Ingredient.of(TagMod.MENU_ITEM)));
        CraftingRecipe bellMenu = new ShapelessRecipe(MaidRestaurant.modLoc("bell_menu"), "", CraftingBookCategory.MISC, ModItems.BELL_MENU.get().getDefaultInstance(), NonNullList.of(Ingredient.EMPTY, Ingredient.of(TagMod.MENU_ITEM), Ingredient.of(ModItems.ORDER_BELL.get())));
        return List.of(menuUnbinding, bellMenu);
    }

    private MenuTransformingRecipeMaker() {
    }
}
