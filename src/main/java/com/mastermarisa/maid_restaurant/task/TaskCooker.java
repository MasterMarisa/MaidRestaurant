package com.mastermarisa.maid_restaurant.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.task.cooker.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class TaskCooker implements IMaidTask {
    public static final ResourceLocation UID = MaidRestaurant.resourceLocation("cooker");

    public ResourceLocation getUid() {
        return UID;
    }

    public ItemStack getIcon() { return ModItems.KITCHEN_SHOVEL.toStack(); }

    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    public boolean enableEating(EntityMaid maid) {
        return false;
    }

    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return InitSounds.MAID_IDLE.get();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList(Pair.of(5,new MaidSearchWorkBlockTask(entityMaid,60,0.4f,3)),
                Pair.of(5,new MaidArriveAndSitTask(1.5D)),
                Pair.of(5,new MaidSearchStorageTask(entityMaid,60,0.4f,1)),
                Pair.of(5,new MaidTakeFromStorageTask(2.0D)));
    }

    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5,new MaidSearchWorkBlockTaskRide(maid,50,3)),
                Pair.of(5,new MaidCookingTask()));
    }
}
