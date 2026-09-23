package com.mastermarisa.maid_restaurant.storage;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.init.ModBlocks;
import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public class CreativeCrateStorage implements IMaidStorage {
    private static final ResourceLocation ID = MaidRestaurant.modLoc("creative_crate");

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() {
        return ModItems.CREATIVE_CRATE.get().getDefaultInstance();
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.CREATIVE_CRATE.get());
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        if (!ingredient.isEmpty()) {
            return List.of(ingredient.getItems()[0].copyWithCount(amount));
        }
        return List.of();
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        return Integer.MAX_VALUE;
    }
}
