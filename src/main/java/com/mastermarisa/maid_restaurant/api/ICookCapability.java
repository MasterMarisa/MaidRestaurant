package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface ICookCapability {
    String getUID();

    ItemStack getIcon();

    RecipeType<?> getRecipeType();

    default List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        return recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
    }

    default int getIngredientCount(Level level, Recipe<?> recipe, int output, IngredientStack stack) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        int multiplier = (int) Math.ceil((double) output / result.getCount());
        return stack.getCount() * multiplier;
    }

    List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node);

    @Nullable
    default BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : zone) {
            if (isValidWorkBlock(level, pos) && !BlockUsageUtils.isUsed(pos)) {
                found.add(pos);
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.stream().min(Comparator.comparingDouble(p -> p.distSqr(maid.blockPosition()))).orElse(null);
    }

    boolean isValidWorkBlock(ServerLevel level, BlockPos pos);

    CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node);

    int getTickInterval();
}
