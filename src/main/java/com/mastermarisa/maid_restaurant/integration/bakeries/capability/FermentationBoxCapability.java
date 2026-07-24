package com.mastermarisa.maid_restaurant.integration.bakeries.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.renyigesai.bakeries.block.fermentation_box.FermentationBoxBlockEntity;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.bakeries.init.BakeriesRecipeTypes;
import com.renyigesai.bakeries.recipe.FermentationBoxRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class FermentationBoxCapability implements ICookCapability {
    public static final String UID = "fermentation_box";

    public static void register() {
        CapabilityRegistry.register(new FermentationBoxCapability());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return BakeriesItems.FERMENTATION_BOX.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return BakeriesRecipeTypes.FERMENTATION_BOX.get(); }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        FermentationBoxRecipe recipe = (FermentationBoxRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe != null && level.getBlockEntity(pos) instanceof FermentationBoxBlockEntity be) {
            Ingredient ingredient = recipe.getIngredients().get(0);
            ItemStack result = recipe.getResultItem(level.registryAccess());
            for (int i = 0; i < 6; i++) {
                ItemStack stack = be.getItem(i);
                if (!stack.isEmpty()) {
                    if (ingredient.test(stack)) {
                        inputs.add(stack);
                    } else if (ItemStack.isSameItem(result, stack)) {
                        inputs.add(ingredient.getItems()[0].copy());
                    }
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FermentationBoxBlockEntity;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof FermentationBoxBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        FermentationBoxRecipe recipe = (FermentationBoxRecipe) node.getRecipe(level.getRecipeManager());
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
                    be.updateBlock();
                    InvUtil.getItemToMaid(maid, be.removeItem(i, 1));
                    maid.swing(InteractionHand.MAIN_HAND);
                    if (required <= 0) {
                        return CookResult.DONE;
                    }
                }
            } else if (ingredient.test(stack)) {
                existedInput++;
            } else {
                freeSlots++;
                be.updateBlock();
                InvUtil.getItemToMaid(maid, be.removeItem(i, 1));
                maid.swing(InteractionHand.MAIN_HAND);
            }
        }

        if (existedInput >= required) {
            return CookResult.PROGRESS;
        }

        int toExtract = Math.min(freeSlots, required - existedInput);
        List<ItemStack> inputs = InvUtil.tryExtract(maidInv, toExtract, ingredient, true, false);
        if (inputs.isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        for (int i = 0; i < 6 && !inputs.isEmpty(); i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) {
                be.updateBlock();
                be.setItem(i, split(inputs));
                be.setNowPerfectTime(level, pos, be);
                int perfectTime = be.getPerfectTime();
                int fermentationTime = be.getFermentationMaxTime();
                if (perfectTime != fermentationTime) {
                    be.addFermentationMaxTime(be, perfectTime - fermentationTime);
                }
                maid.swing(InteractionHand.MAIN_HAND);
            }
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
