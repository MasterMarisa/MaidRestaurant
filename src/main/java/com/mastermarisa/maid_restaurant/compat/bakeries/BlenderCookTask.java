package com.mastermarisa.maid_restaurant.compat.bakeries;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import com.renyigesai.bakeries.common.blocks.blander.BlenderBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.bakeries.common.recipe.BlenderRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BlenderCookTask implements ICookTask {
    public static final String UID = "BlenderCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(BakeriesItems.BLENDER.get()); }

    @Override
    public RecipeType<?> getType() { return BlenderRecipe.Type.INSTANCE; }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        BlenderRecipe recipe = (BlenderRecipe) recipeHolder.value();
        List<StackPredicate> ingredients = new ArrayList<>(recipe.getInputItems().stream().map(StackPredicate::new).toList());
        ItemStack container = recipe.getContainer();
        if (!container.isEmpty()) ingredients.add(StackPredicate.of(container.getItem()));
        return ingredients;
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid, CookRequest.TYPE));
        RecipeHolder<BlenderRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(BlenderRecipe.Type.INSTANCE, request.id));

        if (level.getBlockEntity(pos) instanceof BlenderBlockEntity blender) {
            List<Ingredient> ingredients = recipe.value().getInputItems();
            ItemStackHandler inventory = blender.getInventory();
            ans = ItemHandlerUtils.fromTo(
                    inventory,
                    StackPredicate.of(s -> ingredients.stream().anyMatch(i -> i.test(s))),
                    0,
                    9,
                    true
            );
            ItemStack carrier = inventory.getStackInSlot(9);
            if (recipe.value().getContainer().is(carrier.getItem())) {
                ans.add(carrier.copy());
            }
            ItemStack result = inventory.getStackInSlot(10);
            if (getResult(recipe,level).is(result.getItem()))
                for (Ingredient ingredient : ingredients)
                    ans.add(ingredient.getItems()[0].copy());
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundBlenders = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockEntity(pos) instanceof BlenderBlockEntity && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundBlenders.isEmpty()) {
            return foundBlenders.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlenderBlockEntity;
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        BlenderBlockEntity blender = Objects.requireNonNull((BlenderBlockEntity) level.getBlockEntity(pos));
        ItemStackHandler inventory = blender.getInventory();
        RecipeHolder<BlenderRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(BlenderRecipe.Type.INSTANCE, request.id));
        List<StackPredicate> available = recipe.value().getInputItems().stream().map(StackPredicate::new).toList();

        ItemStack result = inventory.getStackInSlot(10);
        if (request.remain > 0 && result.is(getResult(recipe,level).getItem())) {
            ItemStack remain = ItemHandlerHelper.insertItemStacked(
                    maid.getAvailableInv(false),
                    result.copy(),
                    false
            );
            if (remain.isEmpty()) {
                result.split(1);
                blender.setChanged();
                maid.swing(InteractionHand.OFF_HAND);
                request.remain--;
                return;
            }
        }

        List<ItemStack> input = ItemHandlerUtils.fromTo(inventory, 0, 9, true);
        available = ItemHandlerUtils.getRequired(available,input);
        for (StackPredicate predicate : available) {
            int index = ItemHandlerUtils.findStackSlot(inventory, StackPredicate.of(ItemStack::isEmpty), 0, 9);
            if (index != -1) {
                ItemStack material = ItemHandlerUtils.tryExtractSingleSlot(
                        maid.getAvailableInv(false),
                        1,
                        predicate,
                        true
                );
                if (!material.isEmpty()) {
                    inventory.setStackInSlot(index, material);
                    blender.setChanged();
                    maid.swing(InteractionHand.OFF_HAND);
                }
            }
        }

        ItemStack carrier = inventory.getStackInSlot(9);
        int requested = request.extraData.getInt("left");
        if (carrier.isEmpty() && requested < request.requested) {
            ItemStack toInsert = ItemHandlerUtils.tryExtractSingleSlot(
                    maid.getAvailableInv(false),
                    1,
                    StackPredicate.of(recipe.value().getContainer().getItem()),
                    true
            );
            if (!toInsert.isEmpty()) {
                inventory.setStackInSlot(9, toInsert);
                requested++;
                request.extraData.putInt("left", requested);
                blender.setChanged();
                maid.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(BlenderRecipe.Type.INSTANCE)) {
            ans.add(new RecipeData(
                    holder.id(),
                    getType(),
                    getIcon(),
                    getResult(holder,level)
            ));
        }

        return ans;
    }

    public static void register() { CookTasks.register(new BlenderCookTask()); }
}
