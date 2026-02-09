package com.mastermarisa.maid_restaurant.task.cooktask;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    public ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        return ((SteamerRecipe)recipeHolder.value()).getResult();
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        List<SteamerBlockEntity> steamers = getSteamers(level,pos);
        Optional<CookRequest> request = RequestManager.peekCookRequest(maid);
        if (request.isEmpty()) return ans;
        CookRequest cookRequest = request.get();
        SteamerRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.STEAMER_RECIPE,cookRequest.id).value();

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
        List<BlockPos> foundPots = BlockPosUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
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
        SteamerRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.STEAMER_RECIPE,request.id).value();
        for (SteamerBlockEntity steamer : steamers) {
            if (request.count <= 0) break;
            NonNullList<ItemStack> items = steamer.getItems();
            int[] times = steamer.getCookingTime();
            for (int i = 0; i < (level.getBlockState(steamer.getBlockPos()).getValue(SteamerBlock.HALF) ? 4 : 8); i++) {
                ItemStack item = items.get(i);
                if (item.is(recipe.getResult().getItem())) {
                    if (ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false),item.copy(),false).isEmpty()) {
                        items.get(i).split(1);
                        times[i] = 0;
                        steamer.getCookingProgress()[i] = 0;
                        request.count--;
                        steamer.setChanged();
                        steamer.refresh();
                        maid.swing(InteractionHand.OFF_HAND);
                        if (request.count <= 0) break;
                    }
                } else if (item.isEmpty()) {
                    if (request.requestedCount > 0) {
                        ItemStack ingredient = MaidInvUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(recipe.getIngredient()),true);
                        if (!ingredient.isEmpty()) {
                            request.requestedCount--;
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
    public List<RecipeData> getAllRecipeData() {
        RecipeManager manager = RecipeUtils.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            manager = Minecraft.getInstance().level.getRecipeManager();
        } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            manager = RecipeUtils.getRecipeManager();
        }
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
