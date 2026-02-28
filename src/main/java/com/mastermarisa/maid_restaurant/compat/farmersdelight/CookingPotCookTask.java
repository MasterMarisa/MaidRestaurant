package com.mastermarisa.maid_restaurant.compat.farmersdelight;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CookingPotCookTask implements ICookTask {
    public static final String UID = "CookingPotCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(ModItems.COOKING_POT.get()); }

    @Override
    public RecipeType<?> getType() { return ModRecipeTypes.COOKING.get(); }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        CookingPotRecipe recipe = (CookingPotRecipe) recipeHolder.value();
        List<StackPredicate> predicates = new ArrayList<>(recipe.getIngredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
        ItemStack container = recipe.getOutputContainer();
        if (!container.isEmpty())
            for (int i = 0;i < recipe.getResultItem(level.registryAccess()).getCount();i++)
                predicates.add(StackPredicate.of(recipe.getOutputContainer().getItem()));

        return predicates;
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
            CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid, CookRequest.TYPE));
            CookingPotRecipe recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(ModRecipeTypes.COOKING.get(), request.id)).value();
            ItemStackHandler handler = pot.getInventory();
            ans = ItemHandlerUtils.fromTo(
                    handler,
                    StackPredicate.of(s -> !s.isEmpty()),
                    0,
                    6,
                    true
            );
            ItemStack container = handler.getStackInSlot(7);
            if (!container.isEmpty()) ans.add(container.copy());
            ItemStack output = recipe.getResultItem(level.registryAccess());
            List<Ingredient> ingredients = recipe.getIngredients().stream().filter(i -> i.getItems().length != 0).toList();
            ItemStack meal = handler.getStackInSlot(6);
            int count = 0;
            if (output.is(meal.getItem())) count += meal.getCount();
            ItemStack result = handler.getStackInSlot(8);
            if (output.is(result.getItem())) {
                count += result.getCount();
                ans.add(recipe.getOutputContainer().copyWithCount(result.getCount()));
            }
            if (count != 0)
                for (var i : ingredients)
                    ans.add(i.getItems()[0].copyWithCount(count / output.getCount()));
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundPots = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockEntity(pos) instanceof CookingPotBlockEntity && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundPots.isEmpty()) {
            return foundPots.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.isHeated();
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        CookingPotBlockEntity pot = Objects.requireNonNull((CookingPotBlockEntity) level.getBlockEntity(pos));
        ItemStackHandler handler = pot.getInventory();

        ItemStack meal = pot.getMeal();
        ItemStack container = handler.getStackInSlot(7);
        ItemStack result = handler.getStackInSlot(8);
        CookingPotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipeTypes.COOKING.get(), request.id).value();
        ItemStack output = recipe.getResultItem(level.registryAccess());
        ItemStack carrier = recipe.getOutputContainer();
        if (result.is(output.getItem())) {
            if (result.getCount() / output.getCount() >= request.remain) {
                ItemUtils.getItemToLivingEntity(maid,handler.extractItem(8,request.remain * output.getCount(),false));
                request.remain = 0;
            } else {
                request.remain -= result.getCount() / output.getCount();
                ItemUtils.getItemToLivingEntity(maid,handler.extractItem(8,result.getCount() / output.getCount() * result.getCount(),false));
            }
        } else if (!meal.is(output.getItem()) && container.isEmpty()) {
            ItemStack bowl = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),output.getCount(),StackPredicate.of(carrier.getItem()),true);
            if (!bowl.isEmpty()) {
                handler.setStackInSlot(7,bowl);
            }
        } else {
            List<ItemStack> slots = ItemHandlerUtils.fromTo(
                    handler,
                    StackPredicate.of(s -> !s.isEmpty()),
                    0,
                    6,
                    true
            );

            List<StackPredicate> ingredients = recipe.getIngredients().stream().filter(i -> i.getItems().length != 0).map(StackPredicate::new).toList();
            List<StackPredicate> required = ItemHandlerUtils.getRequired(ingredients,slots);
            for (StackPredicate predicate : required) {
                int index = getFirstEmptySlot(handler);
                if (index != -1) {
                    ItemStack material = ItemHandlerUtils.tryExtractSingleSlot(
                            maid.getAvailableInv(false),
                            1,
                            predicate,
                            true
                    );
                    if (!material.isEmpty()) {
                        handler.setStackInSlot(index,material);
                    }
                }
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(ModRecipeTypes.COOKING.get())) {
            ans.add(new RecipeData(
                    holder.id(),
                    ModRecipeTypes.COOKING.get(),
                    getIcon(),
                    getResult(holder,level)
            ));
        }

        return ans;
    }

    private int getFirstEmptySlot(ItemStackHandler handler) {
        for (int i = 0;i < 6;i++) {
            if (handler.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    public static void register() {
        CookTasks.register(new CookingPotCookTask());
    }
}
