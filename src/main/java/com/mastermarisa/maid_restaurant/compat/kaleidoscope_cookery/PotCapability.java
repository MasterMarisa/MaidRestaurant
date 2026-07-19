package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
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
import com.mastermarisa.maid_restaurant.uitls.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PotCapability implements ICookCapability {
    public static final String UID = "pot";
    private static final Ingredient KITCHEN_SHOVEL = Ingredient.of(TagMod.KITCHEN_SHOVEL);
    private static final Ingredient OIL = Ingredient.of(TagMod.OIL);

    public static void register() {
        CapabilityRegistry.register(new PotCapability());
    }

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModItems.POT.get());
    }

    @Override
    public RecipeType<?> getRecipeType() {
        return ModRecipes.POT_RECIPE;
    }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(ICookCapability.super.getRequiredIngredients(recipe));
        ingredients.add(OIL);
        PotRecipe potRecipe = (PotRecipe) recipe;
        if (!potRecipe.carrier().isEmpty()) {
            for (int i = 0; i < potRecipe.result().getCount(); i++) {
                ingredients.add(potRecipe.carrier());
            }
        }
        ingredients.add(KITCHEN_SHOVEL);
        return ingredients;
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeStep step) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof PotBlockEntity be) {
            inputs.addAll(be.getInputs().stream().filter(s -> !s.isEmpty()).toList());
            if (level.getBlockState(pos).getValue(PotBlock.HAS_OIL)) {
                inputs.add(ModItems.OIL.get().getDefaultInstance());
            }
        }
        return inputs;
    }

    @Override
    @Nullable
    public BlockPos searchWorkBlock(ServerLevel level, AbstractZone zone, EntityMaid maid) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : zone) {
            if (level.getBlockState(pos).is(ModBlocks.POT.get()) && !BlockUsageUtils.isUsed(pos.below())) {
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
        return level.getBlockEntity(pos.above()) instanceof PotBlockEntity pot && pot.hasHeatSource(level);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeStep step) {
        if (!(level.getBlockEntity(pos.above()) instanceof PotBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }
        BlockState state = level.getBlockState(pos.above());

        if (step.getRecipeId() == null) {
            return CookResult.INTERRUPTED;
        }
        Optional<? extends Recipe<?>> recipeOpt = level.getRecipeManager().byKey(step.getRecipeId());
        if (recipeOpt.isEmpty()) {
            return CookResult.INTERRUPTED;
        }
        PotRecipe recipe = (PotRecipe) recipeOpt.get();
        IItemHandler maidInv = maid.getAvailableInv(false);

        switch (be.getStatus()) {
            case 0 -> {
                if (!state.getValue(PotBlock.HAS_OIL)) {
                    List<ItemStack> inputs = ItemUtils.tryExtract(maidInv, 1, Ingredient.of(TagMod.OIL), true, false);
                    if (!inputs.isEmpty()) {
                        be.onPlaceOil(level, maid, inputs.get(0));
                        maid.swing(InteractionHand.MAIN_HAND);
                    }
                } else {
                    if (!be.isEmpty()) {
                        for (var item : be.getInputs()) {
                            if (!item.isEmpty()) {
                                ItemUtils.getItemToMaid(maid, item.copyAndClear());
                            }
                        }
                        be.refresh();
                    }

                    List<IngredientStack> stacks = RecipeCacheBuilder.getIngredientStacks(recipe.getId());
                    stacks = stacks.stream().filter(s -> recipe.getIngredients().contains(s.getIngredient())).toList();
                    for (IngredientStack stack : stacks) {
                        List<ItemStack> itemStacks = ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, true);
                        if (itemStacks.isEmpty()) {
                            return CookResult.INTERRUPTED;
                        }
                    }

                    int shovelIndex = ItemUtils.findStackSlot(maidInv, KITCHEN_SHOVEL);
                    if (shovelIndex == -1) {
                        return CookResult.INTERRUPTED;
                    }
                    MaidUtils.exchangeToHand(maid, InteractionHand.MAIN_HAND, shovelIndex);

                    for (var stack : stacks) {
                        List<ItemStack> inputs = ItemUtils.tryExtract(maidInv, stack.getCount(), stack.getIngredient(), true, false);
                        for (int i = 0; i < inputs.get(0).getCount(); i++) {
                            be.addIngredient(level, maid, inputs.get(0).copyWithCount(1));
                        }
                    }

                    be.onShovelHit(level, maid, maid.getMainHandItem());
                    level.playSound(null, maid.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                    maid.swing(InteractionHand.MAIN_HAND);
                }
            }
            case 1 -> {
                int shovelIndex = ItemUtils.findStackSlot(maidInv, KITCHEN_SHOVEL);
                if (shovelIndex == -1) {
                    return CookResult.INTERRUPTED;
                }
                MaidUtils.exchangeToHand(maid, InteractionHand.MAIN_HAND, shovelIndex);

                be.onShovelHit(level, maid, maid.getMainHandItem());
                level.playSound(null, maid.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                maid.swing(InteractionHand.MAIN_HAND);
            }
            case 2 -> {
                FakePlayer fakePlayer = FakePlayerUtils.getPlayer(level);
                if (!ItemStack.isSameItem(be.getResult(), recipe.result())) {
                    be.reset();
                    return CookResult.PROGRESS;
                }
                if (be.hasCarrier()){
                    List<ItemStack> carriers = ItemUtils.tryExtract(maidInv, be.getResult().getCount(), recipe.carrier(),true, false);
                    if (!carriers.isEmpty()) {
                        for (var stack : carriers) {
                            be.takeOutProduct(level, fakePlayer, stack);
                        }
                        ItemUtils.getAllFromInv(fakePlayer.getInventory(), maid);
                        maid.swing(InteractionHand.MAIN_HAND);
                        return CookResult.DONE;
                    } else {
                        return CookResult.INTERRUPTED;
                    }
                } else {
                    Pig pig = new Pig(EntityType.PIG, level);
                    be.takeOutProduct(level, pig, ModItems.KITCHEN_SHOVEL.get().getDefaultInstance());
                    ItemUtils.getItemToMaid(maid, pig.getMainHandItem());
                    maid.swing(InteractionHand.MAIN_HAND);
                    return CookResult.DONE;
                }
            }
            case 3 -> {
                be.reset();
                maid.swing(InteractionHand.MAIN_HAND);
                return CookResult.INTERRUPTED;
            }
        }
        return CookResult.PROGRESS;
    }
}
