package com.mastermarisa.maid_restaurant.maid;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.maid.task.waiter.MaidServeMealTask;
import com.mastermarisa.maid_restaurant.maid.task.waiter.MaidTakeFromCookTask;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.utils.MaidStorages;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
        return Lists.newArrayList(
                Pair.of(5,new MaidTakeFromCookTask(0.4f,3D)),
                Pair.of(5,new MaidServeMealTask(0.4f,2.0D))
        );
    }

    public static boolean isValidServeBlock(Level level, BlockPos pos) {
        return (level.getBlockState(pos).is(TagBlock.SERVE_MEAL_BLOCK) && level.getBlockState(pos.above()).canBeReplaced()) ||
                MaidStorages.tryGetType(level,pos) != null;
    }
}
