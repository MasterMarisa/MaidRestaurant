package com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.google.common.base.Suppliers;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.FakePlayerUtil;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public int getIngredientCount(Level level, Recipe<?> recipe, int output, IngredientStack stack) {
        ResourceLocation soupBaseId = ((StockpotRecipe) recipe).soupBase();
        if (soupBaseId.equals(ModSoupBases.WATER)) {
            if (IngredientUtil.equals(stack.getIngredient(), SOUP_BASE_MAP.get(soupBaseId))) {
                ItemStack result = recipe.getResultItem(level.registryAccess());
                int multiplier = (int) Math.ceil((double) output / result.getCount());
                return multiplier >= 2 ? 2 : 1;
            }
        }
        if (IngredientUtil.equals(stack.getIngredient(), STOCKPOT_LID.get())) {
            return 1;
        }
        return ICookCapability.super.getIngredientCount(level, recipe, output, stack);
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof StockpotBlockEntity be) {
            inputs.addAll(be.getInputs().stream().filter(s -> !s.isEmpty()).toList());
            if (SOUP_BASE_MAP.containsKey(be.getSoupBaseId())) {
                inputs.add(SOUP_BASE_MAP.get(be.getSoupBaseId()).getItems()[0].copyWithCount(1));
            }
            if (level.getBlockState(pos).getValue(StockpotBlock.HAS_LID)) {
                inputs.add(ModItems.STOCKPOT_LID.get().getDefaultInstance());
            }
            StockpotRecipe recipe = (StockpotRecipe) node.getRecipe(level.getRecipeManager());
            if (recipe != null) {
                Ingredient carrier = recipe.carrier();
                if (!carrier.isEmpty() && be.getStatus() == 3) {
                    int count = recipe.result().getCount() - be.getTakeoutCount();
                    if (count != 0) {
                        inputs.add(carrier.getItems()[0].copyWithCount(count));
                    }
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StockpotBlockEntity be && be.hasHeatSource(level);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof StockpotBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        StockpotRecipe recipe = (StockpotRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }
        IItemHandler maidInv = maid.getAvailableInv(false);

        switch (be.getStatus()) {
            case 0 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos, be);
                } else {
                    Ingredient ingredient = getSoupBaseIngredient(recipe.soupBase());
                    if (recipe.soupBase().equals(ModSoupBases.WATER)) {
                        int count = InvUtil.count(maidInv, getSoupBaseIngredient(recipe.soupBase()));
                        if (count >= 2) {
                            FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);
                            be.addSoupBase(level,fakePlayer, Items.WATER_BUCKET.getDefaultInstance());
                            fakePlayer.getInventory().clearContent();
                            maid.swing(InteractionHand.MAIN_HAND);
                            return CookResult.PROGRESS;
                        }
                    }

                    List<ItemStack> stacks = InvUtil.tryExtract(maidInv, 1, ingredient, true, false);
                    if (stacks.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    be.addSoupBase(level, maid, stacks.get(0));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 1 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos, be);
                } else {
                    if (!be.isEmpty()) {
                        for (var item : be.getInputs()) {
                            if (!item.isEmpty()) {
                                InvUtil.getItemToMaid(maid, item.copyAndClear());
                            }
                        }
                        be.refresh();
                    }

                    if (!recipe.soupBase().equals(be.getSoupBaseId())) {
                        level.setBlockEntity(new StockpotBlockEntity(pos, level.getBlockState(pos)));
                        return CookResult.PROGRESS;
                    }

                    List<IngredientStack> stacks = RecipeCacheBuilder.getIngredientStacks(recipe.getId());
                    stacks = stacks.stream().filter(s -> recipe.getIngredients().contains(s.getIngredient())).toList();
                    for (IngredientStack stack : stacks) {
                        List<ItemStack> itemStacks = InvUtil.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, true);
                        if (itemStacks.isEmpty()) {
                            return CookResult.INTERRUPTED;
                        }
                    }

                    List<ItemStack> lid = InvUtil.tryExtract(maidInv, 1, STOCKPOT_LID.get(), true, false);
                    if (lid.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }

                    for (var stack : stacks) {
                        List<ItemStack> inputs = InvUtil.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, false);
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
                    List<ItemStack> lid = InvUtil.tryExtract(maidInv, 1, STOCKPOT_LID.get(), true, false);
                    if (lid.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    be.onLitClick(level, maid, lid.get(0));
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 3 -> {
                if (be.hasLid()) {
                    takeLid(level, maid, pos, be);
                } else {
                    FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);
                    if (!ItemStack.isSameItem(recipe.result(), be.getResult())) {
                        level.setBlockEntity(new StockpotBlockEntity(pos, level.getBlockState(pos)));
                        return CookResult.PROGRESS;
                    }
                    if (recipe.carrier().isEmpty()) {
                        for (int i = 0; i < recipe.result().getCount(); i++) {
                            be.takeOutProduct(level, fakePlayer, ItemStack.EMPTY);
                        }
                        InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
                        maid.swing(InteractionHand.MAIN_HAND);
                        if (InvUtil.contains(maidInv, node.getIngredient(), node.getCount())) {
                            return CookResult.DONE;
                        } else {
                            return CookResult.PROGRESS;
                        }
                    }
                    List<ItemStack> carriers = InvUtil.tryExtract(maidInv, recipe.result().getCount(), recipe.carrier(), true, false);
                    if (!carriers.isEmpty()) {
                        for (var stack : carriers) {
                            for (int i = 0; i < stack.getCount(); i++) {
                                be.takeOutProduct(level, fakePlayer, stack.copyWithCount(1));
                            }
                        }
                        InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
                        maid.swing(InteractionHand.MAIN_HAND);
                        if (InvUtil.contains(maidInv, node.getIngredient(), node.getCount())) {
                            return CookResult.DONE;
                        } else {
                            return CookResult.PROGRESS;
                        }
                    }
                    return CookResult.INTERRUPTED;
                }
            }
        }
        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 20;
    }

    private void takeLid(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot) {
        ItemStack lid = pot.getLidItem().isEmpty() ? (ModItems.STOCKPOT_LID.get()).getDefaultInstance() : pot.getLidItem().copy();
        pot.setLidItem(ItemStack.EMPTY);
        pot.setChanged();
        level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(StockpotBlock.HAS_LID, false));
        InvUtil.getItemToMaid(maid, lid);
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
