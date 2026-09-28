package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.List;

public interface ICookCapability {
    ResourceLocation getID();

    ItemStack getIcon();

    RecipeType<?> getRecipeType();

    default List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        return recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
    }

    default int getIngredientCount(Recipe<?> recipe, int output, IngredientStack stack, RegistryAccess registries) {
        ItemStack result = recipe.getResultItem(registries);
        int multiplier = (int) Math.ceil((double) output / result.getCount());
        return stack.getCount() * multiplier;
    }

    List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node);

    @Nullable
    default BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        BlockPos center = maid.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : zone) {
            if (!isValidWorkBlock(level, pos)) continue;
            if (BlockUsageUtil.isUsed(pos)) continue;

            double dist = pos.distSqr(center);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
    }

    boolean isValidWorkBlock(ServerLevel level, BlockPos pos);

    CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node);

    int getTickInterval();
}
