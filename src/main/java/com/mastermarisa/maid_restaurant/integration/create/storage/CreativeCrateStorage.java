package com.mastermarisa.maid_restaurant.integration.create.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public class CreativeCrateStorage implements IMaidStorage {
    private static final ResourceLocation ID = Create.asResource("creative_crate");

    public static void register() {
        StorageRegistry.register(new CreativeCrateStorage());
    }

    @Override
    public ResourceLocation getID() { return ID; }

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
        if (ingredient.getItems().length == 0) {
            return List.of();
        }
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
