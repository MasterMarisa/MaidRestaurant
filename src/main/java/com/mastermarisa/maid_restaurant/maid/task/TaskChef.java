package com.mastermarisa.maid_restaurant.maid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.maid.behavior.chef.MaidApproachWorkBlockTask;
import com.mastermarisa.maid_restaurant.maid.behavior.chef.MaidExecuteCookStepTask;
import com.mastermarisa.maid_restaurant.maid.behavior.chef.MaidGatherMaterialTask;
import com.mastermarisa.maid_restaurant.maid.behavior.chef.MaidStoreDishTask;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class TaskChef implements IMaidTask {
    public static final ResourceLocation UID = MaidRestaurant.modLoc("chef");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ModItems.CHEF_LICENSE.get().getDefaultInstance();
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
        return Lists.newArrayList(
                Pair.of(5, new MaidGatherMaterialTask(60, 0.4F, 2.5D)),
                Pair.of(5, new MaidApproachWorkBlockTask(60, 0.4F, 1.5D)),
                Pair.of(5, new MaidExecuteCookStepTask(1.5D)),
                Pair.of(5, new MaidStoreDishTask(60, 0.4F, 2.5D))
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return Lists.newArrayList();
    }
}
