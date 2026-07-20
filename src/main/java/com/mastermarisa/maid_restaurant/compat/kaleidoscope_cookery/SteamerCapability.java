package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
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

public class SteamerCapability implements ICookCapability {
    public static final String UID = "steamer";

    public static void register() {
        CapabilityRegistry.register(new SteamerCapability());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return ModItems.STEAMER.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return ModRecipes.STEAMER_RECIPE; }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        SteamerRecipe recipe = (SteamerRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe != null) {
            Ingredient ingredient = recipe.getIngredient();
            ItemStack result = recipe.getResult();
            for (int i = 0; i < 4; i++) {
                if (level.getBlockEntity(pos.above(i)) instanceof SteamerBlockEntity be) {
                    for (int j = 0; j < be.getItems().size(); j++) {
                        ItemStack stack = be.getItems().get(i);
                        if (ingredient.test(stack)) {
                            inputs.add(stack);
                        } else if (ItemStack.isSameItem(result, stack)) {
                            inputs.add(ingredient.getItems()[0].copy());
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SteamerBlockEntity be && be.hasHeatSource(level);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        List<SteamerBlockEntity> steamers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (level.getBlockEntity(pos.above(i)) instanceof SteamerBlockEntity be) {
                steamers.add(be);
            } else {
                break;
            }
        }
        if (steamers.isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        SteamerRecipe recipe = (SteamerRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }

        int input = 0;
        int output = node.getCount() - ItemUtils.count(maidInv, Ingredient.of(recipe.getResult()));
        if (output <= 0) {
            return CookResult.DONE;
        }

        for (var be : steamers) {
            int size = be.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
            for (int i = 0; i < size; i++) {
                ItemStack stack = be.getItems().get(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (ItemStack.isSameItem(recipe.getResult(), stack)) {
                    if (ItemHandlerHelper.insertItem(maidInv, stack, true).isEmpty()) {
                        output--;
                        ItemUtils.getItemToMaid(maid, removeItem(be, i));
                        maid.swing(InteractionHand.MAIN_HAND);
                        if (output <= 0) {
                            return CookResult.DONE;
                        }
                    }
                } else if (recipe.getIngredient().test(stack)) {
                    input++;
                } else {
                    ItemUtils.getItemToMaid(maid, removeItem(be, i));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
        }

        if (input >= output) {
            return CookResult.PROGRESS;
        }

        List<ItemStack> inputs = ItemUtils.tryExtract(maidInv, output - input, recipe.getIngredient(), true, false);
        if (inputs.isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        for (var be : steamers) {
            int size = be.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
            for (int i = 0; i < size; i++) {
                if (be.getItems().get(i).isEmpty()) {
                    be.getItems().set(i, split(inputs));
                    be.getCookingProgress()[i] = 0;
                    be.getCookingTime()[i] = recipe.getCookTick();
                    if (inputs.isEmpty()) {
                        be.refresh();
                        return CookResult.PROGRESS;
                    }
                }
            }
            be.refresh();
        }
        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 40;
    }

    private static ItemStack removeItem(SteamerBlockEntity be, int index) {
        ItemStack stack = be.getItems().get(index).copyAndClear();
        be.getCookingTime()[index] = 0;
        be.getCookingProgress()[index] = 0;
        be.refresh();
        return stack;
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
