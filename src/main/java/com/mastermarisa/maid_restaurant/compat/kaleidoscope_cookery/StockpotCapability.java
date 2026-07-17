package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.google.common.base.Suppliers;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import com.mastermarisa.maid_restaurant.uitls.FakePlayerUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class StockpotCapability implements ICookCapability {
    public static final String UID = "StockpotCookTask";
    private static final Map<ResourceLocation, Ingredient> SOUP_BASE_MAP = new LinkedHashMap<>();
    private static final Supplier<Ingredient> STOCKPOT_LID = Suppliers.memoize(() -> Ingredient.of(ModItems.STOCKPOT_LID.get()));

    public static void register() {
        CapabilityRegistry.register(new StockpotCapability());
    }

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() { return ModItems.STOCKPOT.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() {
        return ModRecipes.STOCKPOT_RECIPE;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : zone) {
            if (level.getBlockState(pos).is(ModBlocks.STOCKPOT.get()) && !BlockUsageUtils.isUsed(pos.below())) {
                found.add(pos);
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.stream().map(BlockPos::below).min(Comparator.comparingDouble(p -> p.distSqr(maid.blockPosition()))).orElse(null);
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos.above()) instanceof StockpotBlockEntity stockpot && stockpot.hasHeatSource(level);
    }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(ICookCapability.super.getRequiredIngredients(recipe));
        StockpotRecipe stockpotRecipe = (StockpotRecipe) recipe;
        ingredients.add(getSoupBaseIngredient(stockpotRecipe.soupBase()));
        if (!stockpotRecipe.carrier().isEmpty()) {
            for (int i = 0; i < stockpotRecipe.result().getCount(); i++) {
                ingredients.add(stockpotRecipe.carrier());
            }
        }
        ingredients.add(STOCKPOT_LID.get());
        return ingredients;
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeStep step) {
        if (!(level.getBlockEntity(pos.above()) instanceof StockpotBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        if (step.getRecipeId() == null) {
            return CookResult.INTERRUPTED;
        }
        Optional<? extends Recipe<?>> recipeOpt = level.getRecipeManager().byKey(step.getRecipeId());
        if (recipeOpt.isEmpty()) {
            return CookResult.INTERRUPTED;
        }
        StockpotRecipe recipe = (StockpotRecipe) recipeOpt.get();
        IItemHandler maidInv = maid.getAvailableInv(false);

        switch (be.getStatus()) {
            case 0 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos.above(), be);
                } else {
                    Ingredient ingredient = getSoupBaseIngredient(recipe.soupBase());
                    if (recipe.soupBase().equals(ModSoupBases.WATER)) {
                        int count = ItemUtils.count(maidInv, getSoupBaseIngredient(recipe.soupBase()));
                        if (count >= 2) {
                            FakePlayer fakePlayer = FakePlayerUtils.getPlayer(level);
                            be.addSoupBase(level,fakePlayer, Items.WATER_BUCKET.getDefaultInstance());
                            fakePlayer.getInventory().clearContent();
                            maid.swing(InteractionHand.MAIN_HAND);
                            return CookResult.PROGRESS;
                        }
                    }

                    List<ItemStack> stacks = ItemUtils.tryExtract(maidInv, 1, ingredient, true, false);
                    if (stacks.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    be.addSoupBase(level, maid, stacks.get(0));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 1 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos.above(), be);
                } else {
                    List<IngredientStack> stacks = RecipeCacheBuilder.getIngredientStacks(recipe.getId());
                    stacks = stacks.stream().filter(s -> recipe.getIngredients().contains(s.getIngredient())).toList();
                    for (IngredientStack stack : stacks) {
                        List<ItemStack> itemStacks = ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, true);
                        if (itemStacks.isEmpty()) {
                            return CookResult.INTERRUPTED;
                        }
                    }

                    List<ItemStack> lid = ItemUtils.tryExtract(maidInv, 1, STOCKPOT_LID.get(), true, false);
                    if (lid.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }

                    for (var stack : stacks) {
                        List<ItemStack> inputs = ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, false);
                        for (int i = 0; i < inputs.get(0).getCount(); i++) {
                            be.addIngredient(level, maid, inputs.get(0).copyWithCount(1));
                        }
                    }
                    be.onLitClick(level, maid, lid.get(0));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 2 -> {
                if (!be.hasLid()) {
                    List<ItemStack> lid = ItemUtils.tryExtract(maidInv, 1, STOCKPOT_LID.get(), true, false);
                    if (lid.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    be.onLitClick(level, maid, lid.get(0));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 3 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos.above(), be);
                } else {
                    if (recipe.carrier().isEmpty()) {
                        be.takeOutProduct(level, maid, ItemStack.EMPTY);
                        maid.swing(InteractionHand.MAIN_HAND);
                        return CookResult.DONE;
                    }
                    List<ItemStack> carriers = ItemUtils.tryExtract(maidInv, recipe.result().getCount(), recipe.carrier(), true, false);
                    if (!carriers.isEmpty()) {
                        FakePlayer fakePlayer = FakePlayerUtils.getPlayer(level);
                        for (var stack : carriers) {
                            for (int i = 0; i < stack.getCount(); i++) {
                                be.takeOutProduct(level, fakePlayer, stack.copyWithCount(1));
                            }
                        }
                        ItemUtils.getAllFromInv(fakePlayer.getInventory(), maid);
                        maid.swing(InteractionHand.MAIN_HAND);
                        return CookResult.DONE;
                    }
                    return CookResult.INTERRUPTED;
                }
            }
        }
        return CookResult.PROGRESS;
    }

    private void takeLid(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot) {
        ItemStack lid = pot.getLidItem().isEmpty() ? (ModItems.STOCKPOT_LID.get()).getDefaultInstance() : pot.getLidItem().copy();
        pot.setLidItem(ItemStack.EMPTY);
        pot.setChanged();
        level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(StockpotBlock.HAS_LID, false));
        ItemUtils.getItemToMaid(maid, lid);
        maid.playSound(SoundEvents.LANTERN_BREAK, 0.5F, 0.5F);
        maid.swing(InteractionHand.OFF_HAND);
    }

    private Ingredient getSoupBaseIngredient(ResourceLocation id) {
        if (SOUP_BASE_MAP.containsKey(id)) {
            return SOUP_BASE_MAP.get(id);
        }
        List<ItemStack> itemStacks = new ArrayList<>();
        ISoupBase soupBase = SoupBaseManager.getSoupBase(id);
        for (var item : ForgeRegistries.ITEMS.getValues()) {
            if (soupBase.isSoupBase(item.getDefaultInstance())) {
                itemStacks.add(item.getDefaultInstance());
            }
        }
        Ingredient ingredient = Ingredient.of(itemStacks.stream());
        SOUP_BASE_MAP.put(id, ingredient);
        return ingredient;
    }
}
