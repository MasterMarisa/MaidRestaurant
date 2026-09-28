package com.mastermarisa.maid_restaurant.compat.kaleidoscope_cookery.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.world.LevelRecipeLookup;
import com.mastermarisa.maid_restaurant.core.world.MaidWorldView;
import com.mastermarisa.maid_restaurant.core.world.WorldContext;
import com.mastermarisa.maid_restaurant.uitls.FakePlayerUtil;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.List;

public class PotCapability implements ICookCapability {
    public static final ResourceLocation ID = new ResourceLocation("kaleidoscope_cookery", "pot");
    private static final Ingredient KITCHEN_SHOVEL = Ingredient.of(TagMod.KITCHEN_SHOVEL);
    private static final Ingredient OIL = Ingredient.of(TagMod.OIL);

    public static void register() {
        CapabilityRegistry.register(new PotCapability());
    }

    @Override
    public ResourceLocation getID() {
        return ID;
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
        PotRecipe potRecipe = (PotRecipe) recipe;
        ingredients.add(OIL);
        if (!potRecipe.carrier().isEmpty()) {
            for (int i = 0; i < potRecipe.result().getCount(); i++) {
                ingredients.add(potRecipe.carrier());
            }
        }
        ingredients.add(KITCHEN_SHOVEL);
        return ingredients;
    }

    @Override
    public int getIngredientCount(Recipe<?> recipe, int output, IngredientStack stack, RegistryAccess registries) {
        if (IngredientUtil.equals(stack.getIngredient(), KITCHEN_SHOVEL)) return 1;
        return ICookCapability.super.getIngredientCount(recipe, output, stack, registries);
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
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
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.hasHeatSource(level);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof PotBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        PotRecipe recipe = (PotRecipe) node.getRecipe(LevelRecipeLookup.of(level));
        if (recipe == null) return CookResult.INTERRUPTED;

        return switch (be.getStatus()) {
            case 0 -> cookAddOilOrIngredients(level, maid, pos, be, recipe);
            case 1 -> cookShovelHit(level, maid, be);
            case 2 -> cookTakeOutProduct(level, maid, be, recipe, node);
            case 3 -> cookReset(level, maid, be);
            default -> CookResult.INTERRUPTED;
        };
    }

    private CookResult cookAddOilOrIngredients(ServerLevel level, EntityMaid maid, BlockPos pos,
                                               PotBlockEntity be, PotRecipe recipe) {
        BlockState state = level.getBlockState(pos);
        IItemHandler maidInv = maid.getAvailableInv(false);

        if (!state.getValue(PotBlock.HAS_OIL)) {
            List<ItemStack> inputs = InvUtil.extractFull(maidInv, 1, Ingredient.of(TagMod.OIL), false);
            if (!inputs.isEmpty()) {
                be.onPlaceOil(level, maid, inputs.get(0));
                maid.swing(InteractionHand.MAIN_HAND);
            }
            return CookResult.PROGRESS;
        }

        if (!be.isEmpty()) {
            for (var item : be.getInputs()) {
                if (!item.isEmpty()) {
                    InvUtil.getItemToMaid(maid, item.copyAndClear());
                }
            }
            be.refresh();
            maid.swing(InteractionHand.MAIN_HAND);
        }

        if (!equipShovel(maid, maidInv)) {
            return CookResult.INTERRUPTED;
        }

        List<IngredientStack> required = new ArrayList<>();
        for (var stack : RecipeCacheBuilder.getIngredientStacks(recipe.getId())) {
            if (recipe.getIngredients().contains(stack.getIngredient())) {
                required.add(stack);
            }
        }

        List<ItemStack> extracted = InvUtil.extractAll(maidInv, required, false);
        if (extracted.isEmpty()) return CookResult.INTERRUPTED;

        for (ItemStack itemStack : extracted) {
            for (int i = 0; i < itemStack.getCount(); i++) {
                be.addIngredient(level, maid, itemStack.copyWithCount(1));
            }
        }

        shovelHit(level, maid, be);
        return CookResult.PROGRESS;
    }

    private CookResult cookShovelHit(ServerLevel level, EntityMaid maid, PotBlockEntity be) {
        if (!equipShovel(maid, maid.getAvailableInv(false))) {
            return CookResult.INTERRUPTED;
        }
        shovelHit(level, maid, be);
        return CookResult.PROGRESS;
    }

    private CookResult cookTakeOutProduct(ServerLevel level, EntityMaid maid,
                                          PotBlockEntity be, PotRecipe recipe, RecipeNode node) {
        if (!ItemStack.isSameItem(be.getResult(), recipe.result())) {
            be.reset();
            return CookResult.PROGRESS;
        }

        return be.hasCarrier()
                ? takeOutWithCarrier(level, maid, be, recipe, node)
                : takeOutWithoutCarrier(level, maid, be, node);
    }

    private CookResult cookReset(ServerLevel level, EntityMaid maid, PotBlockEntity be) {
        be.reset();
        maid.swing(InteractionHand.MAIN_HAND);
        return CookResult.INTERRUPTED;
    }

    private CookResult takeOutWithCarrier(ServerLevel level, EntityMaid maid,
                                          PotBlockEntity be, PotRecipe recipe, RecipeNode node) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack carrier = InvUtil.extractSingle(maidInv, be.getResult().getCount(),
                recipe.carrier(), true, false);
        if (carrier.isEmpty()) return CookResult.INTERRUPTED;

        FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);
        be.takeOutProduct(level, fakePlayer, carrier);
        InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
        maid.swing(InteractionHand.MAIN_HAND);

        return node.calculateCount(new WorldContext(MaidWorldView.of(maid), LevelRecipeLookup.of(level))) <= 0 ? CookResult.DONE : CookResult.PROGRESS;
    }

    private CookResult takeOutWithoutCarrier(ServerLevel level, EntityMaid maid,
                                             PotBlockEntity be, RecipeNode node) {
        Pig pig = new Pig(EntityType.PIG, level);
        be.takeOutProduct(level, pig, ModItems.KITCHEN_SHOVEL.get().getDefaultInstance());
        InvUtil.getItemToMaid(maid, pig.getMainHandItem());
        maid.swing(InteractionHand.MAIN_HAND);

        return node.calculateCount(new WorldContext(MaidWorldView.of(maid), LevelRecipeLookup.of(level))) <= 0 ? CookResult.DONE : CookResult.PROGRESS;
    }

    private boolean equipShovel(EntityMaid maid, IItemHandler maidInv) {
        int shovelIndex = InvUtil.findSlot(maidInv, KITCHEN_SHOVEL);
        if (shovelIndex == -1) return false;

        if (!KITCHEN_SHOVEL.test(maid.getMainHandItem())) {
            InvUtil.exchangeToHand(maid, InteractionHand.MAIN_HAND, shovelIndex);
        }
        return true;
    }

    private void shovelHit(ServerLevel level, EntityMaid maid, PotBlockEntity be) {
        be.onShovelHit(level, maid, maid.getMainHandItem());
        level.playSound(null, maid.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 1.0F,
                (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        maid.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public int getTickInterval() {
        return 20;
    }
}
