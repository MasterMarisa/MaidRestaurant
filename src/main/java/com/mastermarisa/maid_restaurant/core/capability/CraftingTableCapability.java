package com.mastermarisa.maid_restaurant.core.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

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
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        return List.of();
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        Recipe<?> recipe = node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        List<IngredientStack> ingredients = RecipeCacheBuilder.getIngredientStacks(recipe.getId());

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

        ItemStack remainder = ItemHandlerHelper.insertItem(maidInv, result.copy(), true);
        if (remainder.isEmpty()) {
            ItemHandlerHelper.insertItemStacked(maidInv, result.copy(), false);
            maid.swing(InteractionHand.MAIN_HAND);
            if (ItemUtils.contains(maidInv, node.getIngredient(), node.getCount())) {
                return CookResult.DONE;
            } else {
                return CookResult.PROGRESS;
            }
        }

        return CookResult.INTERRUPTED;
    }

    @Override
    public int getTickInterval() {
        return 1;
    }
}
