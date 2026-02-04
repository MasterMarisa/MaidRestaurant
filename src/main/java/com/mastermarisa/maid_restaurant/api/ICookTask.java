package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.List;

public interface ICookTask {
    String getUID();

    ItemStack getIcon();

    RecipeType<?> getType();

    List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder);

    List<StackPredicate> getKitchenWares();

    ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder);

    @Nullable
    BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange);

    boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos);

    void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request);

    List<RecipeData> getAllRecipeData();
}
