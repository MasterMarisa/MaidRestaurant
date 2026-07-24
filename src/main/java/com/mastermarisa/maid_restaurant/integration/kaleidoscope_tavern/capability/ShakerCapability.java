package com.mastermarisa.maid_restaurant.integration.kaleidoscope_tavern.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.google.common.base.Suppliers;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShakerCapability implements ICookCapability {
    public static final String UID = "shaker";
    private static final Supplier<Ingredient> EMPTY_GLASSWARE = Suppliers.memoize(() -> Ingredient.of(ModItems.EMPTY_GLASSWARE.get()));
    private static final Supplier<Ingredient> SHAKER = Suppliers.memoize(() -> Ingredient.of(ModItems.SHAKER.get()));

    public static void register() {
        CapabilityRegistry.register(new ShakerCapability());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return ModItems.SHAKER.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return ModRecipes.SHAKER_RECIPE; }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(ICookCapability.super.getRequiredIngredients(recipe));
        ingredients.add(EMPTY_GLASSWARE.get());
        ingredients.add(SHAKER.get());
        return ingredients;
    }

    @Override
    public int getIngredientCount(Level level, Recipe<?> recipe, int output, IngredientStack stack) {
        if (IngredientUtil.equals(SHAKER.get(), stack.getIngredient())) {
            return 1;
        }
        return ICookCapability.super.getIngredientCount(level, recipe, output, stack);
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        return List.of();
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        return maid.blockPosition().above();
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return true;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        IItemHandler maidInv = maid.getAvailableInv(false);

        if (!maid.getMainHandItem().is(ModItems.SHAKER.get())) {
            int index = InvUtil.findSlot(maidInv, SHAKER.get());
            if (index == -1) {
                return CookResult.INTERRUPTED;
            }
            InvUtil.exchangeToHand(maid, InteractionHand.MAIN_HAND, index);
        }
        maid.stopUsingItem();

        ShakerRecipe recipe = (ShakerRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }

        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack itemInHand = maid.getMainHandItem();
        if (ShakerItem.hasResult(itemInHand)) {
            if (InvUtil.tryExtract(maidInv, 1, EMPTY_GLASSWARE.get(), true).isEmpty()) {
                return CookResult.INTERRUPTED;
            }
            InvUtil.getItemToMaid(maid, ShakerItem.getResult(itemInHand));
            ShakerItem.removeAll(itemInHand);
            if (node.calculateCount(level, maid) <= 0) {
                return CookResult.DONE;
            }
        }

        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            if (InvUtil.tryExtract(maidInv, 1, ingredient, true, true).isEmpty()) {
                return CookResult.INTERRUPTED;
            }
        }

        int index = 0;
        ItemStackHandler items;
        if (ShakerItem.hasStorage(itemInHand)) {
            items = ShakerItem.getStorage(itemInHand);
        } else {
            items = new ItemStackHandler(3);
        }

        for (var ingredient : recipe.getIngredients()) {
            if (index >= 3 || ingredient.isEmpty()) continue;
            ItemStack stack = InvUtil.tryExtract(maidInv, 1, ingredient, true, false).get(0);
            items.setStackInSlot(index++, stack);
        }
        ShakerItem.setStorage(itemInHand, items);
        ShakerItem.setResult(itemInHand, result.copy());
        maid.startUsingItem(InteractionHand.MAIN_HAND);

        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 95;
    }
}
