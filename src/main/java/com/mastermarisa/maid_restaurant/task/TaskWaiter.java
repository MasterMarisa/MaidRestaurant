package com.mastermarisa.maid_restaurant.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.task.waiter.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class TaskWaiter implements IMaidTask {
    public static final ResourceLocation UID = MaidRestaurant.resourceLocation("waiter");

    public ResourceLocation getUid() {
        return UID;
    }

    public ItemStack getIcon() { return ModItems.FRUIT_BASKET.toStack(); }

    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return InitSounds.MAID_IDLE.get();
    }

    public boolean enableEating(EntityMaid maid) {
        return false;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        maid.setPickup(false);
        return Lists.newArrayList(Pair.of(5,new MaidSearchTableTask(0.4f)),
                Pair.of(5,new MaidServeMealTask(2.0D)),
                Pair.of(5,new MaidArriveAndDropMealTask(2.0D)),
                Pair.of(5,new MaidWalkToCookerTask(1.5D)),
                Pair.of(5,new MaidTakeFromCookerTask(2.0D)));
    }
}
