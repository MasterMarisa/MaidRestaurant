package com.mastermarisa.maid_restaurant.recipe;

import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientStack{
    private final Ingredient ingredient;
    private int count;

    public IngredientStack() {
        this.ingredient = Ingredient.EMPTY;
        this.count = 1;
    }

    public IngredientStack(Ingredient ingredient) {
        this.ingredient = ingredient;
        this.count = 1;
    }

    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public boolean isEmpty() {
        return ingredient.isEmpty() || count <= 0;
    }

    public boolean test(ItemStack itemStack) {
        return ingredient.test(itemStack);
    }

    public boolean is(Ingredient ingredient) {
        return IngredientUtil.equals(this.ingredient, ingredient);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ItemStack[] getItems() {
        return ingredient.getItems();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }
}
