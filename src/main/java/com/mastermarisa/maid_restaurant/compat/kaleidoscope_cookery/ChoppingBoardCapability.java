package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ChoppingBoardBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChoppingBoardCapability implements ICookCapability {
    public static final String UID = "chopping_board";
    private static final Ingredient KITCHEN_KNIFE = Ingredient.of(TagMod.KITCHEN_KNIFE);
    private static Method RESET_BOARD_DATA_METHOD = null;

    public static void register() {
        CapabilityRegistry.register(new ChoppingBoardCapability());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return ModItems.CHOPPING_BOARD.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return ModRecipes.CHOPPING_BOARD_RECIPE; }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(ICookCapability.super.getRequiredIngredients(recipe));
        ingredients.add(KITCHEN_KNIFE);
        return ingredients;
    }

    @Override
    public int getIngredientCount(Level level, Recipe<?> recipe, int output, IngredientStack stack) {
        if (ItemUtils.equals(stack.getIngredient(), KITCHEN_KNIFE)) {
            return 1;
        }
        return ICookCapability.super.getIngredientCount(level, recipe, output, stack);
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity be) {
            if (!be.getCurrentCutStack().isEmpty()) {
                inputs.add(be.getCurrentCutStack());
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        ChoppingBoardRecipe recipe = (ChoppingBoardRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }
        IItemHandler maidInv = maid.getAvailableInv(false);

        if (be.getCurrentCutStack().isEmpty()) {
            List<ItemStack> inputs = ItemUtils.tryExtract(maidInv, 1, recipe.getIngredient(), true, false);
            if (!inputs.isEmpty()) {
                be.onPutItem(level, maid, inputs.get(0));
                maid.swing(InteractionHand.MAIN_HAND);
                return CookResult.PROGRESS;
            }
            return CookResult.INTERRUPTED;
        }

        if (!recipe.getIngredient().test(be.getCurrentCutStack())) {
            ItemUtils.getItemToMaid(maid, be.getCurrentCutStack().copy());
            callResetBoardData(be);
            maid.swing(InteractionHand.MAIN_HAND);
            return CookResult.PROGRESS;
        }

        int index = ItemUtils.findStackSlot(maidInv, KITCHEN_KNIFE);
        if (index == -1) {
            return CookResult.INTERRUPTED;
        }
        if (!KITCHEN_KNIFE.test(maid.getMainHandItem())) {
            MaidUtils.exchangeToHand(maid, InteractionHand.MAIN_HAND, index);
        }

        if (be.getCurrentCutCount() < be.getMaxCutCount()) {
            be.onCutItem(level, maid, maid.getMainHandItem());
            maid.swing(InteractionHand.MAIN_HAND);
            return CookResult.PROGRESS;
        } else {
            ItemUtils.getItemToMaid(maid, recipe.getResultItem(level.registryAccess()).copy());
            callResetBoardData(be);
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 2.0F + level.random.nextFloat() * 0.2F);
            maid.swing(InteractionHand.MAIN_HAND);
            if (ItemUtils.contains(maidInv, node.getOutput(), node.getCount())) {
                return CookResult.DONE;
            } else {
                return CookResult.PROGRESS;
            }
        }
    }

    @Override
    public int getTickInterval() {
        return 5;
    }

    private static void callResetBoardData(ChoppingBoardBlockEntity board) {
        try {
            if (RESET_BOARD_DATA_METHOD == null) {
                RESET_BOARD_DATA_METHOD = ChoppingBoardBlockEntity.class.getDeclaredMethod("resetBoardData");
                RESET_BOARD_DATA_METHOD.setAccessible(true);
            }
            RESET_BOARD_DATA_METHOD.invoke(board);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke resetBoardData", e);
        }
    }
}
