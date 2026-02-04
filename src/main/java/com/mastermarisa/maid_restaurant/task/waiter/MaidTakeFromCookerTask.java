package com.mastermarisa.maid_restaurant.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequest;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidTakeFromCookerTask extends Behavior<EntityMaid> {
    private final double closeEnoughDist;

    public MaidTakeFromCookerTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return BehaviorUtils.getType(maid) == TargetType.COOKER_POS.type && RequestManager.peekServeRequest(maid).map(request -> {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
                return maid.distanceToSqr(cooker) <= Math.pow(closeEnoughDist,2.0D);
            }
            return false;
        }).orElse(false);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ServeRequest request = RequestManager.peekServeRequest(maid).get();
        if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
            MaidInvUtils.tryTakeFrom(cooker.getAvailableInv(false),maid.getAvailableInv(false),StackPredicate.of(request.toServe.getItem()),request.toServe.getCount());
        }

        BehaviorUtils.eraseTargetPos(maid);
    }
}
