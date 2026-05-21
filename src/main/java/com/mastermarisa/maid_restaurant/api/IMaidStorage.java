package com.mastermarisa.maid_restaurant.api;

import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMaidStorage {
    String getUID();

    ItemStack getIcon();

    boolean isValid(Level level, BlockPos pos);

    List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate);

    ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate);

    int count(Level level, BlockPos pos, Ingredient ingredient);

    @Nullable
    default BlockPos searchContaining(ServerLevel level, AbstractZone zone, Ingredient ingredient, int minAmount) {
        for (BlockPos pos : zone) {
            if (isValid(level, pos)) {
                int count = count(level, pos, ingredient);
                if (count >= minAmount) {
                    return pos;
                }
            }
        }
        return null;
    }
}
