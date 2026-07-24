package com.mastermarisa.maid_restaurant.integration.bakeries.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.renyigesai.bakeries.block.blender.BlenderBlockEntity;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.bakeries.recipe.BlenderRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlenderCapability implements ICookCapability {
    public static final String UID = "blender";
    private static final Map<Item, Ingredient> CARRIER_MAP = new ConcurrentHashMap<>();

    public static void register() {
        CapabilityRegistry.register(new BlenderCapability());
    }

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return BakeriesItems.BLENDER.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return BlenderRecipe.Type.INSTANCE; }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(ICookCapability.super.getRequiredIngredients(recipe));
        ItemStack carrier = ((BlenderRecipe) recipe).getContainer();
        if (!carrier.isEmpty()) {
            ingredients.add(CARRIER_MAP.computeIfAbsent(carrier.getItem(), Ingredient::of));
        }
        return ingredients;
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof BlenderBlockEntity be) {
            IItemHandler items = be.getInventory();
            for (int i = 0; i < 10; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    inputs.add(stack);
                }
            }

            ItemStack result = items.getStackInSlot(10);
            BlenderRecipe recipe = (BlenderRecipe) node.getRecipe(level.getRecipeManager());
            if (recipe != null && !result.isEmpty()) {
                for (var ingredient : recipe.getIngredients()) {
                    if (!ingredient.isEmpty()) {
                        inputs.add(ingredient.getItems()[0].copyWithCount(result.getCount()));
                    }
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlenderBlockEntity;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof BlenderBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        BlenderRecipe recipe = (BlenderRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }

        int required = node.calculateCount(level, maid);
        if (required <= 0) {
            return CookResult.DONE;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStackHandler items = be.getInventory();

        ItemStack result = items.extractItem(10, 64, false);
        ItemStack restItem = ItemHandlerHelper.insertItemStacked(maidInv, result, false);
        if (!restItem.isEmpty()) {
            ItemEntity dropItem = maid.spawnAtLocation(restItem);
            if (dropItem != null) {
                dropItem.setPickUpDelay(0);
            }
        }
        be.setChanged();

        if (node.getIngredient().test(result)) {
            required -= result.getCount() - restItem.getCount();
            if (required <= 0) {
                return CookResult.DONE;
            }
        }

        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        List<Ingredient> ingredients = getRequiredIngredients(recipe);
        boolean shouldUpdate = RecipeMatcher.findMatches(inputs, ingredients) == null;

        if (!shouldUpdate) {
            return CookResult.PROGRESS;
        }

        for (int i = 0; i < 10; i++) {
            ItemStack stack = be.removeItem(i, 64);
            if (!stack.isEmpty()) {
                InvUtil.getItemToMaid(maid, stack);
                be.setChanged();
            }
        }

        for (var ingredient : ingredients) {
            List<ItemStack> itemStacks = InvUtil.tryExtract(maidInv, 1, ingredient, true, true);
            if (itemStacks.isEmpty()) {
                return CookResult.INTERRUPTED;
            }
        }

        for (int i = 0; i < ingredients.size() - 1; i++) {
            Ingredient ingredient = ingredients.get(i);
            items.setStackInSlot(i, InvUtil.tryExtract(maidInv, 1, ingredient, true, false).get(0));
        }

        Ingredient ingredient = ingredients.get(ingredients.size() - 1);
        ItemStack stack = InvUtil.tryExtract(maidInv, 1, ingredient, true, false).get(0);
        if (recipe.getContainer().isEmpty()) {
            items.setStackInSlot(ingredients.size() - 1, stack);
        } else {
            items.setStackInSlot(9, stack);
        }
        be.setChanged();

        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 40;
    }
}
