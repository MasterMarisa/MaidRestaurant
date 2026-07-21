package com.mastermarisa.maid_restaurant.compat.create.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public class CreativeCrateStorage implements IMaidStorage {
    private static final String UID = "creative_crate_storage";

    public static void register() {
        StorageRegistry.register(new CreativeCrateStorage());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() {
        return AllBlocks.CREATIVE_CRATE.asStack();
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CreativeCrateBlockEntity;
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        return List.of(ingredient.getItems()[0].copyWithCount(amount));
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
