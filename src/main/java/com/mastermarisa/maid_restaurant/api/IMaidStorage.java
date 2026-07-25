package com.mastermarisa.maid_restaurant.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IMaidStorage {
    ResourceLocation getID();

    ItemStack getIcon();

    default int getPriority() { return 5; }

    boolean isValid(Level level, BlockPos pos);

    List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate);

    ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate);

    int count(Level level, BlockPos pos, Ingredient ingredient);
}
