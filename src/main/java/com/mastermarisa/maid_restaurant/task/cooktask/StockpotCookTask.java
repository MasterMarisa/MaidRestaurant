package com.mastermarisa.maid_restaurant.task.cooktask;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModPoi;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoupBases;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.mixin.StockpotBlockEntityAccessor;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StockpotCookTask implements ICookTask {
    public static final String UID = "StockpotCookTask";
    public static final List<String> blackList;

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ModItems.STOCKPOT.toStack();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.STOCKPOT_RECIPE;
    }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        StockpotRecipe recipe = (StockpotRecipe) recipeHolder.value();
        List<StackPredicate> predicates = new ArrayList<>(recipe.getIngredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
        if (!recipe.carrier().isEmpty())
            for (int i = 0;i < recipe.result().getCount();i++)
                predicates.add(StackPredicate.of(recipe.carrier()));
        predicates.add(StackPredicate.of((stack)-> SoupBaseManager.getSoupBase(recipe.soupBase()).isSoupBase(stack)));

        return predicates;
    }

    @Override
    public List<StackPredicate> getKitchenWares() {
        return List.of();
    }

    @Override
    public ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        return ((StockpotRecipe) recipeHolder.value()).result();
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = level.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange((type)-> type.value().equals(ModPoi.STOCKPOT.get()), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter((pos)-> BlockUsageManager.getUserCount(pos) <= 0).min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StockpotBlockEntity pot && pot.hasHeatSource(level);
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        StockpotBlockEntity pot = Objects.requireNonNull((StockpotBlockEntity) level.getBlockEntity(pos));
        switch (pot.getStatus()) {
            case 0:
                tickState0(level,maid,pos,pot,request);
                break;
            case 1:
                tickState1(level,maid,pos,pot,request);
                break;
            case 2:
                tickState2(level,maid,pos,pot,request);
                break;
            case 3:
                tickState3(level,maid,pos,pot,request);
                break;
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData() {
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : RecipeUtils.getRecipeManager().getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE)) {
            if (!blackList.contains(holder.id().toString()))
                ans.add(new RecipeData(holder.id(),ModRecipes.STOCKPOT_RECIPE,getIcon(),holder.value().result()));
        }
        return ans;
    }

    private void tickState0(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid())
            takeLid(level,maid,pos,pot);
        else {
            StockpotRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            ResourceLocation soupBase = recipe.soupBase();
            ISoupBase iSoupBase = SoupBaseManager.getSoupBase(soupBase);

            if (soupBase.equals(ModSoupBases.WATER)) {
                int count = MaidInvUtils.count(maid.getAvailableInv(false),StackPredicate.of(iSoupBase::isSoupBase));
                if (count >= 2) {
                    StockpotBlockEntityAccessor accessor = (StockpotBlockEntityAccessor) pot;
                    accessor.setSoupBaseId(ModSoupBases.WATER);
                    accessor.setStatus(1);
                    pot.refresh();
                    maid.swing(InteractionHand.OFF_HAND);
                    return;
                }
            }

            List<ItemStack> buckets = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,StackPredicate.of(iSoupBase::isSoupBase),true);
            if (!buckets.isEmpty()) {
                pot.addSoupBase(level,maid,buckets.getFirst());
                maid.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    private void tickState1(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid())
            takeLid(level,maid,pos,pot);
        else {
            StockpotRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            List<StackPredicate> required = new ArrayList<>(recipe.ingredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
            required = MaidInvUtils.getRequired(required,pot.getInputs());
            if (required.isEmpty()) {
                List<ItemStack> lids = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,StackPredicate.of(ModItems.STOCKPOT_LID.asItem()),true);
                if (!lids.isEmpty()) {
                    pot.onLitClick(level,maid,lids.getFirst());
                    maid.swing(InteractionHand.OFF_HAND);
                }
            } else {
                for (StackPredicate ingredient : required) {
                    List<ItemStack> items = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,ingredient,true);
                    if (!items.isEmpty()) {
                        pot.addIngredient(level,maid,items.getFirst());
                        maid.swing(InteractionHand.OFF_HAND);
                    }
                }
            }
        }
    }

    protected void tickState2(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (!pot.hasLid()) {
            List<ItemStack> lids = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,StackPredicate.of(ModItems.STOCKPOT_LID.asItem()),true);
            if (!lids.isEmpty()) {
                pot.onLitClick(level,maid,lids.getFirst());
                maid.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    protected void tickState3(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid()) {
            takeLid(level,maid,pos,pot);
        } else {
            StockpotRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            List<ItemStack> carriers = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,StackPredicate.of(recipe.carrier()),true);
            if (!carriers.isEmpty()) {
                pot.takeOutProduct(level,maid,carriers.getFirst());
                maid.swing(InteractionHand.OFF_HAND);
                if (pot.getTakeoutCount() == 0)
                    request.count--;
            }
        }
    }

    private void takeLid(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot) {
        ItemStack lid = pot.getLidItem().isEmpty() ? (ModItems.STOCKPOT_LID.get()).getDefaultInstance() : pot.getLidItem().copy();
        pot.setLidItem(ItemStack.EMPTY);
        pot.setChanged();
        level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(StockpotBlock.HAS_LID, false));
        maid.playSound(SoundEvents.LANTERN_BREAK, 0.5F, 0.5F);
        ItemUtils.getItemToLivingEntity(maid,lid);
        maid.swing(InteractionHand.OFF_HAND);
    }

    static {
        blackList = new ArrayList<>();
        for (int i = 2;i <= 9;i++) {
            blackList.add("kaleidoscope_cookery:stockpot/dumpling_count_" + i);
        }
    }
}
