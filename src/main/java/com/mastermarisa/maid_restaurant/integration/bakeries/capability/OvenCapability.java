package com.mastermarisa.maid_restaurant.integration.bakeries.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.renyigesai.bakeries.block.oven.OvenBlockEntity;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.bakeries.recipe.oven.OvenRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class OvenCapability implements ICookCapability {
    public static final ResourceLocation ID = new ResourceLocation("bakeries", "oven");

    public static void register() {
        CapabilityRegistry.register(new OvenCapability());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return BakeriesItems.OVEN.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return OvenRecipe.Type.INSTANCE; }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        OvenRecipe recipe = (OvenRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe != null && level.getBlockEntity(pos) instanceof OvenBlockEntity be) {
            Ingredient ingredient = recipe.getIngredients().get(0);
            ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
            for (int i = 0; i < 6; i++) {
                ItemStack stack = be.getItem(i);
                if (ingredient.test(stack)) {
                    inputs.add(stack);
                } else if (ItemStack.isSameItem(stack, result)) {
                    inputs.add(ingredient.getItems()[0].copy());
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof OvenBlockEntity;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof OvenBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        OvenRecipe recipe = (OvenRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }

        int required = node.calculateCount(level, maid);
        if (required <= 0) {
            return CookResult.DONE;
        }
        int existedInput = 0;
        int freeSlots = 0;

        IItemHandler maidInv = maid.getAvailableInv(false);
        Ingredient ingredient = recipe.getIngredients().get(0);
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        boolean changed = false;
        for (int i = 0; i < 6; i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) {
                freeSlots++;
                continue;
            }

            if (ItemStack.isSameItem(stack, result)) {
                if (ItemHandlerHelper.insertItem(maidInv, stack, true).isEmpty()) {
                    required--;
                    freeSlots++;
                    changed = true;
                    be.cooking_times[i] = 0;
                    be.max_cooking_times[i] = 0;
                    InvUtil.getItemToMaid(maid, be.removeItem(i, 1));
                    maid.swing(InteractionHand.MAIN_HAND);
                    if (required <= 0) {
                        OvenBlockEntity.updateBlock(be);
                        return CookResult.DONE;
                    }
                }
            } else if (ingredient.test(stack)) {
                existedInput++;
            } else {
                freeSlots++;
                changed = true;
                be.cooking_times[i] = 0;
                be.max_cooking_times[i] = 0;
                InvUtil.getItemToMaid(maid, be.removeItem(i, 1));
                maid.swing(InteractionHand.MAIN_HAND);
            }
        }

        if (existedInput >= required) {
            if (changed) {
                OvenBlockEntity.updateBlock(be);
            }
            return CookResult.PROGRESS;
        }

        int toExtract = Math.min(freeSlots, required - existedInput);
        List<ItemStack> inputs = InvUtil.tryExtract(maidInv, toExtract, ingredient, true, false);
        if (inputs.isEmpty()) {
            if (changed) {
                OvenBlockEntity.updateBlock(be);
            }
            return CookResult.INTERRUPTED;
        }

        for (int i = 0; i < 6 && !inputs.isEmpty(); i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) {
                changed = true;
                be.setItem(i, split(inputs));
                be.cooking_times[i] = 0;
                be.setTemperature(be, recipe.getPerfectTemperature());
                maid.swing(InteractionHand.MAIN_HAND);
            }
        }

        if (changed) {
            OvenBlockEntity.updateBlock(be);
        }

        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 40;
    }

    private static ItemStack split(List<ItemStack> items) {
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(0).split(1);
        if (items.get(0).isEmpty()) {
            items.remove(0);
        }
        return result;
    }
}
