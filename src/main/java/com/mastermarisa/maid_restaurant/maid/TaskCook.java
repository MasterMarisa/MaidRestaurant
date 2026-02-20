package com.mastermarisa.maid_restaurant.maid;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.config.RestaurantConfig;
import com.mastermarisa.maid_restaurant.maid.task.cook.MaidApproachCookBlockTask;
import com.mastermarisa.maid_restaurant.maid.task.cook.MaidCookingTask;
import com.mastermarisa.maid_restaurant.maid.task.cook.MaidGetFromStorageTask;
import com.mastermarisa.maid_restaurant.maid.task.cook.MaidSearchCookBlockTask;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class TaskCook implements IMaidTask {
    public static final ResourceLocation UID = MaidRestaurant.resourceLocation("cook");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() { return ModItems.KITCHEN_SHOVEL.toStack(); }

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
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList(
                Pair.of(5,new MaidGetFromStorageTask(entityMaid,60,0.4f,2,3.0D)),
                Pair.of(5,new MaidApproachCookBlockTask(entityMaid,60,0.4f,2, RestaurantConfig.SIT_WHILE_COOKING() ? 2.0D : 0.25D)),
                Pair.of(5,new MaidCookingTask()),
                Pair.of(5,new MaidSearchCookBlockTask(entityMaid,60,2))
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList();
    }
}
