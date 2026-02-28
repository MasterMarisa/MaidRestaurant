package com.mastermarisa.maid_restaurant.compat.bakeries;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import com.renyigesai.bakeries.common.blocks.oven.OvenBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import com.renyigesai.bakeries.common.recipe.oven.OvenRecipe;
import com.renyigesai.bakeries.common.recipe.oven.OvenRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OvenCookTask implements ICookTask {
    public static final String UID = "OvenCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(BakeriesItems.OVEN.get()); }

    @Override
    public RecipeType<?> getType() { return OvenRecipe.Type.INSTANCE; }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid, CookRequest.TYPE));
        RecipeHolder<OvenRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(OvenRecipe.Type.INSTANCE, request.id));

        if (level.getBlockEntity(pos) instanceof OvenBlockEntity oven) {
            for (int i = 0;i < 6;i++) {
                ItemStack itemStack = oven.getItem(i);
                if (recipe.value().matches(new OvenRecipeInput(itemStack), level))
                    ans.add(itemStack.copy());
                else if (getResult(recipe, level).is(itemStack.getItem())) {
                    List<Ingredient> available = recipe.value().getIngredients().stream().filter(s -> !s.isEmpty() && s.getItems().length > 0).toList();
                    if (!available.isEmpty())
                        ans.add(available.getFirst().getItems()[0].copy());
                }
            }
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundOvens = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockEntity(pos) instanceof OvenBlockEntity && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundOvens.isEmpty()) {
            return foundOvens.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof OvenBlockEntity;
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        OvenBlockEntity oven = Objects.requireNonNull((OvenBlockEntity) level.getBlockEntity(pos));
        RecipeHolder<OvenRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(OvenRecipe.Type.INSTANCE, request.id));
        List<Ingredient> available = recipe.value().getIngredients().stream().filter(s -> !s.isEmpty() && s.getItems().length > 0).toList();
        if (available.isEmpty()) return;

        for (int i = 0;i < 6;i++) {
            ItemStack itemStack = oven.getItem(i);
            int requested = request.extraData.getInt("left");
            if (itemStack.isEmpty() && requested < request.requested) {
                ItemStack ingredient = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1, StackPredicate.of(available.getFirst()),true);
                if (!ingredient.isEmpty()) {
                    requested++;
                    request.extraData.putInt("left",requested);
                    oven.cooking_times[i] = 0;
                    oven.max_cooking_times[i] = 0;
                    oven.setItem(i,ingredient);
                    oven.setTemperature(recipe.value().getPerfectTemperature());
                    oven.setChanged();
                    maid.swing(InteractionHand.OFF_HAND);
                }
            } else if (request.remain > 0 && !itemStack.isEmpty() && itemStack.is(getResult(recipe,level).getItem())) {
                ItemStack result = oven.getItem(i);
                if (ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false),result.copyWithCount(1),false).isEmpty()) {
                    oven.removeItem(i,1);
                    oven.cooking_times[i] = 0;
                    oven.max_cooking_times[i] = 0;
                    maid.swing(InteractionHand.OFF_HAND);
                    request.remain--;
                    if (request.remain <= 0) break;
                }
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(OvenRecipe.Type.INSTANCE)) {
            ans.add(new RecipeData(holder.id(), getType(), getIcon(), getResult(holder,level)));
        }

        return ans;
    }

    public static void register() {
        CookTasks.register(new OvenCookTask());
    }
}
