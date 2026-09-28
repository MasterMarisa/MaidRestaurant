package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability;

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
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
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
    public static final ResourceLocation ID = new ResourceLocation("kaleidoscope_cookery", "stockpot");
    private static final Map<ResourceLocation, Ingredient> SOUP_BASE_MAP = new LinkedHashMap<>();
    private static final Supplier<Ingredient> STOCKPOT_LID = Suppliers.memoize(() -> Ingredient.of(ModItems.STOCKPOT_LID.get()));

    public static void register() {
        CapabilityRegistry.register(new StockpotCapability());
    }

    @Override
    public ResourceLocation getID() { return ID; }

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
        Ingredient ingredient = stack.getIngredient();
        if (soupBaseId.equals(ModSoupBases.WATER)) {
            if (IngredientUtil.equals(ingredient, SOUP_BASE_MAP.get(soupBaseId))) {
                ItemStack result = recipe.getResultItem(level.registryAccess());
                int multiplier = (int) Math.ceil((double) output / result.getCount());
                return multiplier >= 2 ? 2 : 1;
            }
        }
        if (IngredientUtil.equals(ingredient, STOCKPOT_LID.get())) {
            return 1;
        }
        return ICookCapability.super.getIngredientCount(level, recipe, output, stack);
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof StockpotBlockEntity be) {
            for (ItemStack itemStack : be.getInputs()) {
                if (!itemStack.isEmpty()) {
                    inputs.add(itemStack);
                }
            }

            if (be.getStatus() != 0) {
                Ingredient ingredient = getSoupBaseIngredient(be.getSoupBaseId());
                if (!ingredient.isEmpty()) {
                    inputs.add(ingredient.getItems()[0].copyWithCount(1));
                }
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
        if (recipe == null) return CookResult.INTERRUPTED;

        if (be.hasLid() && be.getStatus() != 2) {
            takeLid(level, maid, pos, be);
            return CookResult.PROGRESS;
        }

        return switch (be.getStatus()) {
            case 0 -> cookAddSoupBase(level, maid, pos, be, recipe);
            case 1 -> cookAddIngredients(level, maid, pos, be, recipe);
            case 2 -> cookLid(level, maid, pos, be);
            case 3 -> cookTakeOut(level, maid, pos, be, recipe, node);
            default -> CookResult.INTERRUPTED;
        };
    }

    @Override
    public int getTickInterval() {
        return 20;
    }

    private CookResult cookAddSoupBase(ServerLevel level, EntityMaid maid, BlockPos pos,
                                       StockpotBlockEntity be, StockpotRecipe recipe) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        Ingredient ingredient = getSoupBaseIngredient(recipe.soupBase());

        if (recipe.soupBase().equals(ModSoupBases.WATER)) {
            int count = InvUtil.count(maidInv, getSoupBaseIngredient(recipe.soupBase()));

            if (count >= 2) {
                FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);
                be.addSoupBase(level, fakePlayer, Items.WATER_BUCKET.getDefaultInstance());
                maid.swing(InteractionHand.MAIN_HAND);
                return CookResult.PROGRESS;
            }
        }

        List<ItemStack> stacks = InvUtil.extractFull(maidInv, 1, ingredient, false);
        if (stacks.isEmpty()) return CookResult.INTERRUPTED;

        be.addSoupBase(level, maid, stacks.get(0));
        maid.swing(InteractionHand.MAIN_HAND);
        return CookResult.PROGRESS;
    }

    private CookResult cookAddIngredients(ServerLevel level, EntityMaid maid, BlockPos pos,
                                          StockpotBlockEntity be, StockpotRecipe recipe) {
        IItemHandler maidInv = maid.getAvailableInv(false);

        if (!be.isEmpty()) {
            for (var item : be.getInputs()) {
                if (!item.isEmpty()) {
                    InvUtil.getItemToMaid(maid, item.copyAndClear());
                }
            }
            be.refresh();
            maid.swing(InteractionHand.MAIN_HAND);
        }

        if (!recipe.soupBase().equals(be.getSoupBaseId())) {
            maid.swing(InteractionHand.MAIN_HAND);
            level.setBlockEntity(new StockpotBlockEntity(pos, level.getBlockState(pos)));
            return CookResult.PROGRESS;
        }

        List<IngredientStack> required = new ArrayList<>();
        for (var stack : RecipeCacheBuilder.getIngredientStacks(recipe.getId())) {
            if (recipe.getIngredients().contains(stack.getIngredient())) {
                required.add(stack);
            }
        }

        List<ItemStack> lidItem = InvUtil.extractFull(maidInv, 1, STOCKPOT_LID.get(), true);
        if (lidItem.isEmpty()) return CookResult.INTERRUPTED;

        List<ItemStack> extracted = InvUtil.extractAll(maidInv, required, false);
        if (extracted.isEmpty()) return CookResult.INTERRUPTED;
        lidItem = InvUtil.extractFull(maidInv, 1, STOCKPOT_LID.get(), false);

        for (ItemStack itemStack : extracted) {
            for (int i = 0; i < itemStack.getCount(); i++) {
                be.addIngredient(level, maid, itemStack.copyWithCount(1));
            }
        }
        be.onLitClick(level, maid, lidItem.get(0));
        maid.swing(InteractionHand.MAIN_HAND);
        return CookResult.PROGRESS;
    }

    private CookResult cookLid(ServerLevel level, EntityMaid maid,
                               BlockPos pos, StockpotBlockEntity be) {
        if (!be.hasLid()) {
            IItemHandler maidInv = maid.getAvailableInv(false);
            List<ItemStack> lidItem = InvUtil.extractFull(maidInv, 1, STOCKPOT_LID.get(), false);
            if (lidItem.isEmpty()) return CookResult.INTERRUPTED;
            be.onLitClick(level, maid, lidItem.get(0));
            maid.swing(InteractionHand.MAIN_HAND);
        }
        return CookResult.PROGRESS;
    }

    private CookResult cookTakeOut(ServerLevel level, EntityMaid maid, BlockPos pos,
                                   StockpotBlockEntity be, StockpotRecipe recipe, RecipeNode node) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);
        List<ItemStack> carriers;
        if (recipe.carrier().isEmpty()) {
            carriers = new ArrayList<>();
            for (int i = 0; i < recipe.result().getCount(); i++) {
                carriers.add(ItemStack.EMPTY);
            }
        } else {
            carriers = InvUtil.extractFull(maidInv, recipe.result().getCount(), recipe.carrier(), false);
            if (carriers.isEmpty()) return CookResult.INTERRUPTED;
        }

        for (ItemStack carrier : carriers) {
            be.takeOutProduct(level, fakePlayer, carrier);
        }

        InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
        maid.swing(InteractionHand.MAIN_HAND);
        return node.calculateCount(level, maid) <= 0 ? CookResult.DONE : CookResult.PROGRESS;
    }

    private void takeLid(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot) {
        ItemStack lidItem = pot.getLidItem();
        if (lidItem.isEmpty()) lidItem = ModItems.STOCKPOT_LID.get().getDefaultInstance();
        pot.setLidItem(ItemStack.EMPTY);
        pot.setChanged();
        maid.swing(InteractionHand.OFF_HAND);
        InvUtil.getItemToMaid(maid, lidItem);
        maid.playSound(SoundEvents.LANTERN_BREAK, 0.5F, 0.5F);
        level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(StockpotBlock.HAS_LID, false));
    }

    private Ingredient getSoupBaseIngredient(ResourceLocation id) {
        return SOUP_BASE_MAP.computeIfAbsent(id, k -> {
            List<ItemStack> itemStacks = new ArrayList<>();
            ISoupBase soupBase = SoupBaseManager.getSoupBase(id);
            for (var item : ForgeRegistries.ITEMS.getValues()) {
                if (soupBase.isSoupBase(item.getDefaultInstance())) {
                    itemStacks.add(item.getDefaultInstance());
                }
            }
            return Ingredient.of(itemStacks.stream());
        });
    }
}
