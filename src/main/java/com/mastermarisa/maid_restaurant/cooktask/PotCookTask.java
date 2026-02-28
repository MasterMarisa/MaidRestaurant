package com.mastermarisa.maid_restaurant.cooktask;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModPoi;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.BlockUsageManager;
import com.mastermarisa.maid_restaurant.utils.ItemHandlerUtils;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder, Level level) {
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
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            ans.addAll(pot.getInputs().stream().filter(s -> !s.isEmpty()).toList());
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
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(ModRecipes.POT_RECIPE)) {
            if (!blackList.contains(holder.id().toString()))
                ans.add(new RecipeData(holder.id(),ModRecipes.POT_RECIPE,getIcon(),holder.value().result()));
        }
        return ans;
    }

    private void tickState0(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        BlockState state = level.getBlockState(pos);
        if (!state.getValue(PotBlock.HAS_OIL)) {
            ItemStack oil = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(TagMod.OIL),true);
            if (!oil.isEmpty()) {
                pot.onPlaceOil(level,maid,oil);
                maid.swing(InteractionHand.OFF_HAND);
            }
        } else {
            PotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.POT_RECIPE,request.id).value();
            List<StackPredicate> required = new ArrayList<>(recipe.ingredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
            required = ItemHandlerUtils.getRequired(required,pot.getInputs());
            if (required.isEmpty()) {
                ItemStack shovel = ItemHandlerUtils.tryGet(maid.getAvailableInv(false),StackPredicate.of(TagMod.KITCHEN_SHOVEL));
                if (!shovel.isEmpty()) {
                    pot.onShovelHit(level,maid,shovel);
                    level.playSound((Player)null, maid.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
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

    private void tickState1(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        ItemStack shovel = ItemHandlerUtils.tryGet(maid.getAvailableInv(false),StackPredicate.of(TagMod.KITCHEN_SHOVEL));
        if (!shovel.isEmpty()) {
            pot.onShovelHit(level,maid,shovel);
            maid.swing(InteractionHand.OFF_HAND);
            level.playSound((Player)null, maid.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        }
    }

    private void tickState2(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        if (pot.hasCarrier()){
            PotRecipe recipe = level.getRecipeManager().byKeyTyped(ModRecipes.POT_RECIPE,request.id).value();
            ItemStack carrier = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),pot.getResult().getCount(),StackPredicate.of(recipe.carrier()),true);
            if (!carrier.isEmpty()) {
                pot.takeOutProduct(level,maid,carrier);
                maid.swing(InteractionHand.OFF_HAND);
                request.remain--;
            }
        } else {
            pot.takeOutProduct(level,maid, new ItemStack(ModItems.KITCHEN_SHOVEL.get()));
            maid.swing(InteractionHand.OFF_HAND);
            request.remain--;
        }
    }

    private void tickState3(ServerLevel level, EntityMaid maid, BlockPos pos, PotBlockEntity pot, CookRequest request) {
        pot.reset();
        maid.swing(InteractionHand.OFF_HAND);
    }

    static {
        blackList = new ArrayList<>(List.of(
                "kaleidoscope_cookery:pot/braised_fish_salmon",
                "kaleidoscope_cookery:pot/braised_fish_salmon_with_rice",
                "kaleidoscope_cookery:pot/braised_fish_cod",
                "kaleidoscope_cookery:pot/braised_fish_cod_with_rice",
                "kaleidoscope_cookery:pot/spicy_chicken_blaze_powder",
                "kaleidoscope_cookery:pot/golden_salad_enchanted_golden_apple",
                "kaleidoscope_cookery:pot/stargazy_pie_cod",
                "kaleidoscope_cookery:pot/stargazy_pie_salmon",
                "kaleidoscope_cookery:pot/sweet_and_sour_ender_pearls_2",
                "kaleidoscope_cookery:pot/sweet_and_sour_ender_pearls_3",
                "kaleidoscope_cookery:pot/spicy_chicken_rice_bowl_blaze_powder",
                "kaleidoscope_cookery:pot/slime_ball_5_to_slime_ball_meal_1",
                "kaleidoscope_cookery:pot/slime_ball_6_to_slime_ball_meal_1",
                "kaleidoscope_cookery:pot/slime_ball_7_to_slime_ball_meal_1",
                "kaleidoscope_cookery:pot/slime_ball_9_to_slime_ball_meal_2",
                "kaleidoscope_cookery:pot/egg_fried_rice_2",
                "kaleidoscope_cookery:pot/egg_fried_rice_3"
        ));
        for (int i = 2;i <= 9;i++) {
            blackList.add("kaleidoscope_cookery:pot/stuffed_dough_food_to_meat_pie_" + i);
            blackList.add("kaleidoscope_cookery:pot/egg_to_fried_egg_" + i);
        }
    }
}
