package com.mastermarisa.maid_restaurant.maid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.capability.CraftingTableCapability;
import com.mastermarisa.maid_restaurant.core.recipe.ContextList;
import com.mastermarisa.maid_restaurant.core.recipe.CookStep;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeExecutionContext;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeNode;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.maid.behavior.chef.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.List;

public class TaskChef implements IMaidTask {
    public static final ResourceLocation UID = MaidRestaurant.resourceLocation("chef");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModItems.KITCHEN_SHOVEL.get());
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return false;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return InitSounds.MAID_IDLE.get();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        test(maid);
        return Lists.newArrayList(
                Pair.of(5, new MaidGatherMaterialTask(60, 0.4F, 3.0D)),
                Pair.of(5, new MaidApproachWorkBlockTask(60, 0.4F, 1.0D)),
                Pair.of(5, new MaidExecuteCookStepTask()),
                Pair.of(5, new MaidSwitchContextTask(60))
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return Lists.newArrayList();
    }

    private static void test(EntityMaid maid) {
        ContextList contextList = ChefScheduler.getContextList(maid);
        contextList.getList().clear();

        RecipeNode planks = new RecipeNode(Items.OAK_PLANKS.getDefaultInstance(), 3, null);
        RecipeNode stone = new RecipeNode(Items.COBBLESTONE.getDefaultInstance(), 4, null);
        RecipeNode iron = new RecipeNode(Items.IRON_INGOT.getDefaultInstance(), 1, null);
        RecipeNode redStone = new RecipeNode(Items.REDSTONE.getDefaultInstance(), 1, null);

        RecipeNode piston = new RecipeNode(Items.PISTON.getDefaultInstance(), 1, null);
        RecipeNode slime_ball = new RecipeNode(Items.SLIME_BALL.getDefaultInstance(), 1, null);

        RecipeNode sticky_piston = new RecipeNode(Items.STICKY_PISTON.getDefaultInstance(), 1, null);

        piston.setCombineStep(new CookStep(CraftingTableCapability.UID, tagWithRecipeId("minecraft:piston")));
        piston.setChildren(List.of(planks, stone, iron, redStone));

        sticky_piston.setCombineStep(new CookStep(CraftingTableCapability.UID, tagWithRecipeId("minecraft:sticky_piston")));
        sticky_piston.setChildren(List.of(slime_ball, piston));

        RecipeExecutionContext context = new RecipeExecutionContext(sticky_piston);
        context.getRoot().computeState();
        contextList.getList().add(context);
        contextList.setCurrentIndex(0);
        maid.setData(ModTaskDataKeys.RESTAURANT_ZONE, new RestaurantZone(
                new BlockPos(0, -60, 1),
                new BlockPos(6, -58, 7)
        ));
    }

    private static CompoundTag tagWithRecipeId(String recipeId) {
        CompoundTag params = new CompoundTag();
        params.putString(CookStep.RECIPE_ID, recipeId);
        return params;
    }
}
