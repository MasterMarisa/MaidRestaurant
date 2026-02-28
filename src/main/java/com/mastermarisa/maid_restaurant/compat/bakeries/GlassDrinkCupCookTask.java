package com.mastermarisa.maid_restaurant.compat.bakeries;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import com.renyigesai.bakeries.common.blocks.glass_drink_cup.GlassDrinkCupBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.bakeries.common.recipe.DrinkRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GlassDrinkCupCookTask implements ICookTask {
    public static final String UID = "GlassDrinkCupCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(BakeriesItems.DRINK_CUP.get()); }

    @Override
    public RecipeType<?> getType() { return DrinkRecipe.Type.INSTANCE; }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipe, Level level) {
        DrinkRecipe drinkRecipe = (DrinkRecipe) recipe.value();
        List<StackPredicate> ans = new ArrayList<>(drinkRecipe.getInputItems().stream().map(StackPredicate::new).toList());
        ans.add(StackPredicate.of(BakeriesItems.DRINK_CUP.get()));
        return ans;
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid, CookRequest.TYPE));
        RecipeHolder<DrinkRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(DrinkRecipe.Type.INSTANCE, request.id));
        List<Ingredient> available = recipe.value().getInputItems();

        if (level.getBlockEntity(pos) instanceof GlassDrinkCupBlockEntity cup) {
            ans.add(new ItemStack(BakeriesItems.DRINK_CUP.get()));
            ItemStackHandler handler = cup.getInventory();
            ans.addAll(ItemHandlerUtils.fromTo(
                    handler,
                    StackPredicate.of(s -> available.stream().anyMatch(i -> i.test(s))),
                    0,
                    4,
                    true
            ));
            ItemStack result = handler.getStackInSlot(4);
            if (result.is(getResult(recipe, level).getItem()))
                for (Ingredient ingredient : available)
                    ans.add(ingredient.getItems()[0]);
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundOvens = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockState(pos.immutable().below()).is(BakeriesBlocks.CUPBOARD.get())
                        && (level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).is(BakeriesBlocks.DRINK_CUP.get()))
                        && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundOvens.isEmpty()) {
            return foundOvens.stream().min(Comparator.comparingDouble(p -> p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockState(pos.immutable().below()).is(BakeriesBlocks.CUPBOARD.get())
                && (level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).is(BakeriesBlocks.DRINK_CUP.get()));
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        if (level.getBlockEntity(pos) instanceof GlassDrinkCupBlockEntity cup) {
            ItemStackHandler handler = cup.getInventory();
            RecipeHolder<DrinkRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(DrinkRecipe.Type.INSTANCE, request.id));
            List<StackPredicate> available = recipe.value().getInputItems().stream().map(StackPredicate::new).toList();

            ItemStack result = handler.getStackInSlot(4);
            if (request.remain > 0 && result.is(getResult(recipe, level).getItem())) {
                ItemStack remain = ItemHandlerHelper.insertItemStacked(
                        maid.getAvailableInv(false),
                        result.copy(),
                        false
                );
                if (remain.isEmpty()) {
                    for (int i = 0;i < 4;i++) handler.setStackInSlot(i,ItemStack.EMPTY);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    maid.swing(InteractionHand.OFF_HAND);
                    request.remain--;
                    return;
                }
            }

            List<ItemStack> input = ItemHandlerUtils.fromTo(handler, 0, 4, true);
            available = ItemHandlerUtils.getRequired(available, input);

            for (StackPredicate predicate : available) {
                int index = ItemHandlerUtils.findStackSlot(handler, StackPredicate.of(ItemStack::isEmpty), 0, 4);
                if (index != -1) {
                    ItemStack material = ItemHandlerUtils.tryExtractSingleSlot(
                            maid.getAvailableInv(false),
                            1,
                            predicate,
                            true
                    );
                    if (!material.isEmpty()) {
                        if (material.hasCraftingRemainingItem()) {
                            ItemUtils.getItemToLivingEntity(maid, material.getCraftingRemainingItem());
                        }
                        handler.setStackInSlot(index, material);
                        maid.swing(InteractionHand.OFF_HAND);
                    }
                }
            }

            cup.forcedRefresh();
            cup.setChanged();
            level.sendBlockUpdated(cup.getBlockPos(), cup.getBlockState(), cup.getBlockState(), 3);
            cup.craftTick();
        } else if (level.getBlockState(pos).canBeReplaced()) {
            ItemStack cup = ItemHandlerUtils.tryExtractSingleSlot(
                    maid.getAvailableInv(false),
                    1,
                    StackPredicate.of(BakeriesItems.DRINK_CUP.get()),
                    true
            );
            if (!cup.isEmpty()) {
                maid.placeItemBlock(
                        InteractionHand.OFF_HAND,
                        pos,
                        DirectionUtils.getHorizontalDirection(pos.getX() - maid.position().x(),pos.getZ() - maid.position().z()),
                        cup
                );
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(DrinkRecipe.Type.INSTANCE)) {
            ans.add(new RecipeData(
                    holder.id(),
                    getType(),
                    getIcon(),
                    getResult(holder,level)
            ));
        }

        return ans;
    }

    public static void register() {
        CookTasks.register(new GlassDrinkCupCookTask());
    }
}
