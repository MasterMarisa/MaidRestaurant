package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface ICookTask {
    String getUID();

    ItemStack getIcon();

    RecipeType<?> getType();

    default List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        return recipeHolder.value().getIngredients().stream().filter(i -> !i.isEmpty() && i.getItems().length > 0).map(StackPredicate::new).collect(Collectors.toList());
    }

    default List<StackPredicate> getKitchenWares() {
        return new ArrayList<>();
    }

    /**
     * Display samples for {@link #getIngredients} and {@link #getKitchenWares}, in the same order and
     * of the same length.
     *
     * <p>Some requirements cannot be recovered from a recipe's ingredient list at all: a stockpot's
     * soup base is a {@code ResourceLocation} and its carrier a separate {@code Ingredient} field
     * rather than entries in {@code getIngredients()}, and a pot's oil exists only in code. UI that
     * wants to name such a requirement as "missing a water bucket" has nowhere to look it up, so each
     * task supplies the sample stack here.
     *
     * <p>Use {@link ItemStack#EMPTY} for a requirement that has no presentable item; consumers must
     * skip empty samples rather than failing. The default implementation covers all requirements that
     * do come from the recipe.
     */
    default List<ItemStack> getIngredientDisplay(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        List<ItemStack> display = new ArrayList<>();
        for (StackPredicate predicate : getIngredients(recipeHolder, level))
            display.add(firstMatching(predicate, recipeHolder));
        for (int i = 0; i < getKitchenWares().size(); i++)
            display.add(ItemStack.EMPTY);

        return display;
    }

    private static ItemStack firstMatching(StackPredicate predicate, RecipeHolder<? extends Recipe<?>> recipeHolder) {
        for (var ingredient : recipeHolder.value().getIngredients()) {
            if (ingredient.isEmpty()) continue;
            for (ItemStack item : ingredient.getItems())
                if (!item.isEmpty() && predicate.test(item)) return item;
        }

        return ItemStack.EMPTY;
    }

    default ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        return recipeHolder.value().getResultItem(level.registryAccess());
    }

    List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid);

    @Nullable
    BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange);

    boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos);

    void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request);

    List<RecipeData> getAllRecipeData(Level level);
}
