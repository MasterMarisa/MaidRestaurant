package com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.google.common.base.Suppliers;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.FakePlayerUtil;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TeapotCapability implements ICookCapability {
    public static final ResourceLocation ID = new ResourceLocation("kaleidoscope_cookery", "teapot");
    private static final Map<ResourceLocation, Ingredient> FLUID_INGREDIENT_MAP = new LinkedHashMap<>();
    private static final Supplier<Ingredient> EMPTY_CUP = Suppliers.memoize(() -> Ingredient.of(ModItems.EMPTY_CUP.get()));

    public static void register() { CapabilityRegistry.register(new TeapotCapability()); }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return ModItems.TEAPOT.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return ModRecipes.TEAPOT_RECIPE; }

    @Override
    public List<Ingredient> getRequiredIngredients(Recipe<?> recipe) {
        TeapotRecipe teapotRecipe = (TeapotRecipe) recipe;
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(getFluidIngredient(teapotRecipe.teaFluid()));
        for (int i = 0; i < teapotRecipe.ingredientCount(); i++) {
            ingredients.add(teapotRecipe.ingredient());
        }
        for (int i = 0; i < 12; i++) {
            ingredients.add(EMPTY_CUP.get());
        }
        return ingredients;
    }

    @Override
    public int getIngredientCount(Level level, Recipe<?> recipe, int output, IngredientStack stack) {
        int multiplier = Mth.positiveCeilDiv(output, 12);
        return stack.getCount() * multiplier;
    }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof TeapotBlockEntity be) {
            if (be.getStatus() != 2) {
                ResourceLocation teaFluidId = be.getTeaFluidId();
                if (!teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(teaFluidId);
                    Item bucket = fluid == null ? Items.WATER_BUCKET : fluid.getBucket();
                    inputs.add(bucket.getDefaultInstance());
                }
                ItemStack input = be.getInput().copy();
                if (!input.isEmpty()) {
                    inputs.add(input);
                }
            } else {
                ItemStack result = be.getResult();
                TeapotRecipe recipe = (TeapotRecipe) node.getRecipe(level.getRecipeManager());
                if (recipe != null && ItemStack.isSameItem(result, recipe.result()) && result.getCount() == 12) {
                    ResourceLocation teaFluidId = recipe.teaFluid();
                    if (!teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                        Fluid fluid = ForgeRegistries.FLUIDS.getValue(teaFluidId);
                        Item bucket = fluid == null ? Items.WATER_BUCKET : fluid.getBucket();
                        inputs.add(bucket.getDefaultInstance());
                    }
                    inputs.add(recipe.ingredient().getItems()[0].copyWithCount(recipe.ingredientCount()));
                }
            }
        }
        return inputs;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TeapotBlockEntity be && be.hasHeatSource(level);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        if (!(level.getBlockEntity(pos) instanceof TeapotBlockEntity be)) {
            return CookResult.INTERRUPTED;
        }

        TeapotRecipe recipe = (TeapotRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }
        IItemHandler maidInv = maid.getAvailableInv(false);
        FakePlayer fakePlayer = FakePlayerUtil.getPlayer(level);

        switch (be.getStatus()) {
            case 0 -> {
                ResourceLocation teaFluidId = be.getTeaFluidId();
                if (!teaFluidId.equals(recipe.teaFluid())) {
                    Ingredient bucket = getFluidIngredient(recipe.teaFluid());
                    List<ItemStack> extracted = InvUtil.tryExtract(maidInv, 1, bucket, true);
                    if (extracted.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    be.addTeaFluid(level, fakePlayer, extracted.get(0));
                    InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
                    maid.swing(InteractionHand.MAIN_HAND);
                }
                ItemStack input = be.getInput();
                if (!recipe.ingredient().test(input)) {
                    be.removeIngredient(level, fakePlayer);
                    InvUtil.getAllFromInv(fakePlayer.getInventory(), maid);
                    List<ItemStack> extracted = InvUtil.tryExtract(maidInv, recipe.ingredientCount(), recipe.ingredient(), true);
                    if (extracted.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    for (ItemStack stack : extracted) {
                        be.insertIngredient(stack);
                    }
                    maid.swing(InteractionHand.MAIN_HAND);
                } else if (input.getCount() < recipe.ingredientCount()) {
                    int count = recipe.ingredientCount() - input.getCount();
                    List<ItemStack> extracted = InvUtil.tryExtract(maidInv, count, Ingredient.of(input), true);
                    if (extracted.isEmpty()) {
                        return CookResult.INTERRUPTED;
                    }
                    for (ItemStack stack : extracted) {
                        be.insertIngredient(stack);
                    }
                    maid.swing(InteractionHand.MAIN_HAND);
                }
                be.refresh();
            }
            case 2 -> {
                ItemStack result = be.getResult();
                if (!ItemStack.isSameItem(result, recipe.result())) {
                    maid.swing(InteractionHand.MAIN_HAND);
                    TeapotBlockEntity teapot = new TeapotBlockEntity(pos, level.getBlockState(pos));
                    level.setBlockEntity(teapot);
                    teapot.refresh();
                    return CookResult.PROGRESS;
                }
                List<ItemStack> extracted = InvUtil.tryExtract(maidInv, result.getCount(), EMPTY_CUP.get(), true);
                if (extracted.isEmpty()) {
                    return CookResult.INTERRUPTED;
                }
                TeapotBlockEntity teapot = new TeapotBlockEntity(pos, level.getBlockState(pos));
                level.setBlockEntity(teapot);
                teapot.refresh();
                InvUtil.getItemToMaid(maid, result.copy());
                maid.swing(InteractionHand.MAIN_HAND);
                if (node.calculateCount(level, maid) <= 0) {
                    return CookResult.DONE;
                } else {
                    return CookResult.PROGRESS;
                }
            }
        }

        return CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 20;
    }

    private Ingredient getFluidIngredient(ResourceLocation key) {
        return FLUID_INGREDIENT_MAP.computeIfAbsent(key, k -> {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(k);
            Item bucket = fluid == null ? Items.WATER_BUCKET : fluid.getBucket();
            return Ingredient.of(bucket);
        });
    }
}
