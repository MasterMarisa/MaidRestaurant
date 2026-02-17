package com.mastermarisa.maid_restaurant.cooktask;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.BlockUsageManager;
import com.mastermarisa.maid_restaurant.utils.ItemHandlerUtils;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import com.mastermarisa.maid_restaurant.utils.SearchUtils;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SteamerCookTask implements ICookTask {
    public static final String UID = "SteamerCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(ModItems.STEAMER.get()); }

    @Override
    public RecipeType<?> getType() { return ModRecipes.STEAMER_RECIPE; }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        return new ArrayList<>(List.of(StackPredicate.of(((SteamerRecipe)recipeHolder.value()).getIngredient())));
    }

    @Override
    public List<StackPredicate> getKitchenWares() {
        return List.of();
    }

    @Override
    public ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        return ((SteamerRecipe)recipeHolder.value()).getResult();
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        List<SteamerBlockEntity> steamers = getSteamers(level,pos);
        CookRequest request = (CookRequest) RequestManager.peek(maid,CookRequest.TYPE);
        if (request == null) return ans;
        SteamerRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STEAMER_RECIPE,request.id).value();

        for (SteamerBlockEntity steamer : steamers) {
            NonNullList<ItemStack> items = steamer.getItems();
            for (int i = 0;i < items.size();i++)
                if (recipe.getIngredient().test(items.get(i)))
                    ans.add(items.get(i));
                else if (items.get(i).is(recipe.getResult().getItem()))
                    ans.add(recipe.getIngredient().getItems()[0]);
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundPots = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockEntity(pos) instanceof SteamerBlockEntity steamer && steamer.hasHeatSource(level) && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundPots.isEmpty()) {
            return foundPots.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SteamerBlockEntity steamer && steamer.hasHeatSource(level);
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        List<SteamerBlockEntity> steamers = getSteamers(level,pos);
        SteamerRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STEAMER_RECIPE,request.id).value();
        for (SteamerBlockEntity steamer : steamers) {
            if (request.remain <= 0) break;
            NonNullList<ItemStack> items = steamer.getItems();
            int[] times = steamer.getCookingTime();
            for (int i = 0; i < (level.getBlockState(steamer.getBlockPos()).getValue(SteamerBlock.HALF) ? 4 : 8); i++) {
                ItemStack item = items.get(i);
                if (item.is(recipe.getResult().getItem())) {
                    if (ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false),item.copy(),false).isEmpty()) {
                        items.get(i).split(1);
                        times[i] = 0;
                        steamer.getCookingProgress()[i] = 0;
                        request.remain--;
                        steamer.setChanged();
                        steamer.refresh();
                        maid.swing(InteractionHand.OFF_HAND);
                        if (request.remain <= 0) break;
                    }
                } else if (item.isEmpty()) {
                    int requested = request.extraData.getInt("left");
                    if (requested < request.requested) {
                        ItemStack ingredient = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1, StackPredicate.of(recipe.getIngredient()),true);
                        if (!ingredient.isEmpty()) {
                            requested++;
                            request.extraData.putInt("left",requested);
                            items.set(i,ingredient);
                            times[i] = recipe.getCookTick();
                            steamer.getCookingProgress()[i] = 0;
                            steamer.setChanged();
                            steamer.refresh();
                            maid.swing(InteractionHand.OFF_HAND);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(ModRecipes.STEAMER_RECIPE)) {
            ans.add(new RecipeData(holder.id(),ModRecipes.STEAMER_RECIPE,getIcon(),holder.value().getResult()));
        }
        return ans;
    }

    private List<SteamerBlockEntity> getSteamers(Level level, BlockPos pos) {
        List<SteamerBlockEntity> steamers = new ArrayList<>();
        pos = pos.immutable();
        for (int i = 0;i < 4;i++) {
            if (level.getBlockEntity(pos) instanceof SteamerBlockEntity steamer) {
                steamers.add(steamer);
                pos = pos.above();
            } else
                break;
        }

        return steamers;
    }
}
