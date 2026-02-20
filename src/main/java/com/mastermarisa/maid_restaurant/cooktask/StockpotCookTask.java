package com.mastermarisa.maid_restaurant.cooktask;

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
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.BlockUsageManager;
import com.mastermarisa.maid_restaurant.utils.FakePlayerUtils;
import com.mastermarisa.maid_restaurant.utils.ItemHandlerUtils;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
        StockpotRecipe recipe = (StockpotRecipe) recipeHolder.value();
        List<StackPredicate> predicates = new ArrayList<>(recipe.getIngredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
        if (!recipe.carrier().isEmpty())
            for (int i = 0;i < recipe.result().getCount();i++)
                predicates.add(StackPredicate.of(recipe.carrier()));
        predicates.add(StackPredicate.of(SoupBaseManager.getSoupBase(recipe.soupBase())::isSoupBase));

        return predicates;
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof StockpotBlockEntity pot) {
            ans.addAll(pot.getInputs().stream().dropWhile(ItemStack::isEmpty).toList());
            if (pot.getSoupBase() != null)
                ans.add(pot.getSoupBase().getDisplayStack());
            if (pot.getStatus() == 3) {
                RecipeHolder<StockpotRecipe> holder = pot.recipe;
                List<Ingredient> ingredients = holder.value().getIngredients().stream().dropWhile(Ingredient::isEmpty).dropWhile(Ingredient::hasNoItems).toList();
                for (var item : ingredients)
                    if (item.getItems().length > 0)
                        ans.add(item.getItems()[0]);
                for (int i = 0;i < holder.value().result().getCount() - pot.getTakeoutCount();i++)
                    ans.add(holder.value().carrier().getItems()[0]);
                ans.add(SoupBaseManager.getSoupBase(holder.value().soupBase()).getDisplayStack());
            }
        }

        return ans;
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
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE)) {
            if (!blackList.contains(holder.id().toString()))
                ans.add(new RecipeData(holder.id(),ModRecipes.STOCKPOT_RECIPE,getIcon(),holder.value().result()));
        }
        return ans;
    }

    private void tickState0(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid())
            takeLid(level,maid,pos,pot);
        else {
            StockpotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            ResourceLocation soupBase = recipe.soupBase();
            ISoupBase iSoupBase = SoupBaseManager.getSoupBase(soupBase);

            if (soupBase.equals(ModSoupBases.WATER)) {
                int count = ItemHandlerUtils.count(maid.getAvailableInv(false),StackPredicate.of(iSoupBase::isSoupBase));
                if (count >= 2) {
                    FakePlayer fakePlayer = FakePlayerUtils.getPlayer(level);
                    pot.addSoupBase(level,fakePlayer,new ItemStack(Items.WATER_BUCKET));
                    fakePlayer.getInventory().clearContent();
                    maid.swing(InteractionHand.OFF_HAND);
                    return;
                }
            }

            ItemStack bucket = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(iSoupBase::isSoupBase),true);
            if (!bucket.isEmpty()) {
                pot.addSoupBase(level,maid,bucket);
                maid.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    private void tickState1(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid())
            takeLid(level,maid,pos,pot);
        else {
            StockpotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            List<StackPredicate> required = new ArrayList<>(recipe.ingredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
            required = ItemHandlerUtils.getRequired(required,pot.getInputs());
            if (required.isEmpty()) {
                ItemStack lid = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(ModItems.STOCKPOT_LID.asItem()),true);
                if (!lid.isEmpty()) {
                    pot.onLitClick(level,maid,lid);
                    maid.swing(InteractionHand.OFF_HAND);
                }
            } else {
                for (StackPredicate ingredient : required) {
                    ItemStack material = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,ingredient,true);
                    if (!material.isEmpty()) {
                        pot.addIngredient(level,maid,material);
                        maid.swing(InteractionHand.OFF_HAND);
                    }
                }
            }
        }
    }

    protected void tickState2(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (!pot.hasLid()) {
            ItemStack lid = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(ModItems.STOCKPOT_LID.asItem()),true);
            if (!lid.isEmpty()) {
                pot.onLitClick(level,maid,lid);
                maid.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    protected void tickState3(ServerLevel level, EntityMaid maid, BlockPos pos, StockpotBlockEntity pot, CookRequest request) {
        if (pot.hasLid()) {
            takeLid(level,maid,pos,pot);
        } else {
            StockpotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.STOCKPOT_RECIPE,request.id).value();
            ItemStack carrier = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(recipe.carrier()),true);
            if (!carrier.isEmpty()) {
                pot.takeOutProduct(level,maid,carrier);
                maid.swing(InteractionHand.OFF_HAND);
                if (pot.getTakeoutCount() == 0)
                    request.remain--;
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
        blackList = new ArrayList<>(List.of(
                "kaleidoscope_cookery:stockpot/seafood_miso_soup_cod",
                "kaleidoscope_cookery:stockpot/seafood_miso_soup_salmon",
                "kaleidoscope_cookery:stockpot/shengjian_mantou_count_2"
        ));
        for (int i = 2;i <= 9;i++) {
            blackList.add("kaleidoscope_cookery:stockpot/dumpling_count_" + i);
        }
    }
}
