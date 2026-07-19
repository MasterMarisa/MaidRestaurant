package com.mastermarisa.maid_restaurant.core.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CraftingTableCapability implements ICookCapability {
    public static final String UID = "crafting_table";

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() { return RecipeType.CRAFTING; }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeStep step) {
        return List.of();
    }

    @Override
    @Nullable
    public BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : zone) {
            if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE) && !BlockUsageUtils.isUsed(pos)) {
                found.add(pos);
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.stream().min(Comparator.comparingDouble(p -> p.distSqr(maid.blockPosition()))).orElse(null);
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeStep step) {
        ItemStack result;
        List<IngredientStack> ingredients;

        if (step.getRecipeId() == null) {
            return CookResult.INTERRUPTED;
        }
        Optional<? extends Recipe<?>> recipeOpt = level.getRecipeManager().byKey(step.getRecipeId());
        if (recipeOpt.isEmpty()) {
            return CookResult.INTERRUPTED;
        }
        Recipe<?> recipe = recipeOpt.get();
        result = recipe.getResultItem(level.registryAccess());
        ingredients = RecipeCacheBuilder.getIngredientStacks(step.getRecipeId());

        if (result.isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        for (IngredientStack stack : ingredients) {
            List<ItemStack> itemStacks = ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, true);
            if (itemStacks.isEmpty()) {
                return CookResult.INTERRUPTED;
            }
        }

        for (var stack : ingredients) {
            ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, false);
        }

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(maidInv, result.copy(), false);
        if (!remainder.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), remainder);
            level.addFreshEntity(itemEntity);
        }

        maid.swing(InteractionHand.MAIN_HAND);
        return CookResult.DONE;
    }
}
