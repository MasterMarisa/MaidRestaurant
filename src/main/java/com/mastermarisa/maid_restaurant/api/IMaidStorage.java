package com.mastermarisa.maid_restaurant.api;

import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
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
    default BlockPos searchContaining(ServerLevel level, RestaurantZone zone, Ingredient ingredient, int minAmount) {
        if (!zone.isValid()) return null;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = zone.getMin().getX(); x <= zone.getMax().getX(); x++) {
            for (int y = zone.getMin().getY(); y <= zone.getMax().getY(); y++) {
                for (int z = zone.getMin().getZ(); z <= zone.getMax().getZ(); z++) {
                    mutable.set(x, y, z);
                    if (isValid(level, mutable)) {
                        int count = count(level, mutable, ingredient);
                        if (count >= minAmount) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}
