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

    default List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        return recipeHolder.value().getIngredients().stream().filter(i -> !i.isEmpty() && i.getItems().length > 0).map(StackPredicate::new).collect(Collectors.toList());
    }

    default List<StackPredicate> getKitchenWares() {
        return new ArrayList<>();
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
