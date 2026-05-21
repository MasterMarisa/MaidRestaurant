package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.List;

public interface ICookCapability {
    String getUID();

    ItemStack getIcon();

    RecipeType<?> getRecipeType();

    @Nullable
    BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid);

    boolean isValidWorkBlock(ServerLevel level, BlockPos pos);

    default List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        return recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
    }

    CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeStep step);
}
