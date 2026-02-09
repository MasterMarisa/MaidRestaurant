package com.mastermarisa.maid_restaurant.task.cooktask;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModPoi;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PotCookTask implements ICookTask {
    public static final String UID = "PotCookTask";
    public static final List<String> blackList;

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ModItems.POT.toStack();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.POT_RECIPE;
    }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        PotRecipe recipe = (PotRecipe) recipeHolder.value();
        List<StackPredicate> predicates = new ArrayList<>(recipe.getIngredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
        if (!recipe.carrier().isEmpty())
            for (int i = 0;i < recipe.result().getCount();i++)
                predicates.add(StackPredicate.of(recipe.carrier()));
        predicates.add(StackPredicate.of(TagMod.OIL));

        return predicates;
    }

    @Override
    public List<StackPredicate> getKitchenWares() {
        return List.of(StackPredicate.of(TagMod.KITCHEN_SHOVEL));
    }

    @Override
    public ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        return ((PotRecipe) recipeHolder.value()).result();
    }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            ans.addAll(pot.getInputs().stream().dropWhile(ItemStack::isEmpty).toList());
            if (level.getBlockState(pos).getValue(PotBlock.HAS_OIL))
                ans.add(new ItemStack(ModItems.OIL.get()));
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos blockPos = maid.getBrainSearchPos();
        PoiManager poiManager = level.getPoiManager();
        int range = (int) maid.getRestrictRadius();
        return poiManager.getInRange((type)-> type.value().equals(ModPoi.POT.get()), blockPos, range, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter((pos)-> BlockUsageManager.getUserCount(pos) <= 0).min(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition()))).orElse(null);
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.hasHeatSource(level);
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        PotBlockEntity pot = Objects.requireNonNull((PotBlockEntity) level.getBlockEntity(pos));
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
        RecipeManager manager = RecipeUtils.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            manager = Minecraft.getInstance().level.getRecipeManager();
        } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            manager = RecipeUtils.getRecipeManager();
        }
        for (var holder : manager.getAllRecipesFor(ModRecipes.POT_RECIPE)) {
            if (!blackList.contains(holder.id().toString()))
                ans.add(new RecipeData(holder.id(),ModRecipes.POT_RECIPE,getIcon(),holder.value().result()));
        }
        return ans;
    }

    private void tickState0(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        BlockState state = level.getBlockState(pos);
        if (!state.getValue(PotBlock.HAS_OIL)) {
            List<ItemStack> oil = MaidInvUtils.tryExtract(maid.getAvailableInv(false),1,StackPredicate.of(TagMod.OIL),true);
            if (!oil.isEmpty()) {
                pot.onPlaceOil(level,maid,oil.getFirst());
                maid.swing(InteractionHand.OFF_HAND);
            }
        } else {
            PotRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.POT_RECIPE,request.id).value();
            List<StackPredicate> required = new ArrayList<>(recipe.ingredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
            required = MaidInvUtils.getRequired(required,pot.getInputs());
            if (required.isEmpty()) {
                ItemStack shovel = MaidInvUtils.tryGet(maid.getAvailableInv(false),StackPredicate.of(TagMod.KITCHEN_SHOVEL));
                if (!shovel.isEmpty()) {
                    pot.onShovelHit(level,maid,shovel);
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

    private void tickState1(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        ItemStack shovel = MaidInvUtils.tryGet(maid.getAvailableInv(false),StackPredicate.of(TagMod.KITCHEN_SHOVEL));
        if (!shovel.isEmpty()) {
            pot.onShovelHit(level,maid,shovel);
            maid.swing(InteractionHand.OFF_HAND);
        }
    }

    private void tickState2(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        if (pot.hasCarrier()){
            PotRecipe recipe = RecipeUtils.getRecipeManager().byKeyTyped(ModRecipes.POT_RECIPE,request.id).value();
            ItemStack carrier = MaidInvUtils.tryExtractSingleSlot(maid.getAvailableInv(false),pot.getResult().getCount(),StackPredicate.of(recipe.carrier()),true);
            if (!carrier.isEmpty()) {
                pot.takeOutProduct(level,maid,carrier);
                maid.swing(InteractionHand.OFF_HAND);
                request.count--;
            }
        } else {
            pot.takeOutProduct(level,maid, new ItemStack(ModItems.KITCHEN_SHOVEL.get()));
            maid.swing(InteractionHand.OFF_HAND);
            request.count--;
        }
    }

    private void tickState3(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        pot.reset();
        maid.swing(InteractionHand.OFF_HAND);
    }

    static {
        blackList = new ArrayList<>(List.of("kaleidoscope_cookery:pot/braised_fish_salmon","kaleidoscope_cookery:pot/braised_fish_salmon_with_rice","kaleidoscope_cookery:pot/braised_fish_cod","kaleidoscope_cookery:pot/braised_fish_cod_with_rice"));
        for (int i = 2;i <= 9;i++) {
            blackList.add("kaleidoscope_cookery:pot/stuffed_dough_food_to_meat_pie_" + i);
            blackList.add("kaleidoscope_cookery:pot/egg_to_fried_egg_" + i);
        }
    }
}
