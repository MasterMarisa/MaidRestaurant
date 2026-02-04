package com.mastermarisa.maid_restaurant.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MaidArriveAndDropMealTask extends Behavior<EntityMaid> {
    private final double closeEnoughDist;

    public MaidArriveAndDropMealTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return BehaviorUtils.getType(maid) == TargetType.DROP_MEAL_POS.type && maid.distanceToSqr(BehaviorUtils.getTargetPos(maid).get().currentPosition()) <= Math.pow(closeEnoughDist, 2.0D);
    }

    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        RequestManager.popServeRequest(maid).ifPresent(request -> {
            List<ItemStack> meals = MaidInvUtils.tryExtract(maid.getAvailableInv(false),request.toServe.getCount(),StackPredicate.of(request.toServe.getItem()),false);
            if (!meals.isEmpty()) {
                BlockPos pos = request.targetTables.getFirst();
                for (ItemStack meal : meals) {
                    ItemEntity itemEntity = new ItemEntity(level,pos.getX(),pos.above().getCenter().y(),pos.getZ(),meal);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        });

        BehaviorUtils.eraseTargetPos(maid);
    }
}
