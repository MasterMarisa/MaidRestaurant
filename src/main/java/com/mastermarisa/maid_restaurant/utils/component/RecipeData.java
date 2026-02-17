package com.mastermarisa.maid_restaurant.utils.component;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeData {
    public ResourceLocation ID;
    public RecipeType<?> type;
    public ItemStack icon;
    public ItemStack result;

    public RecipeData(ResourceLocation ID, RecipeType<?> type, ItemStack icon, ItemStack result) {
        this.ID = ID;
        this.type = type;
        this.icon = icon;
        this.result = result;
    }
}
