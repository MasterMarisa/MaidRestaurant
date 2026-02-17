package com.mastermarisa.maid_restaurant.maid.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.IStep;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.maid.task.base.StepResult;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.utils.BehaviorUtils;
import com.mastermarisa.maid_restaurant.utils.ItemHandlerUtils;
import com.mastermarisa.maid_restaurant.utils.MaidStateManager;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Objects;

public class MaidTakeFromCookTask extends MaidTickRateTask implements IStep {
    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidTakeFromCookTask(float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),5,60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return MaidStateManager.serveState(maid,level) == MaidStateManager.ServeState.TAKING;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ServeRequest request = Objects.requireNonNull((ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE));
        if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook) {
            BehaviorUtils.setTargetPos(maid,new EntityTracker(cooker,false),4);
            BehaviorUtils.setWalkAndLookTargetMemories(maid,cooker.blockPosition(),cooker.blockPosition(),movementSpeed,0);
        }
    }

    @Override
    protected boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        if (request != null && level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook) {
            return maid.distanceToSqr(cooker) > Math.pow(closeEnoughDist,2.0D);
        }
        return false;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level,maid,gameTime)) return;

        ServeRequest request = Objects.requireNonNull((ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE));
        if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook) {
            BehaviorUtils.setWalkAndLookTargetMemories(maid,cooker.blockPosition(),cooker.blockPosition(),movementSpeed,0);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        if (request != null && level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook) {
             if (maid.distanceToSqr(cooker) <= Math.pow(closeEnoughDist,2.0D)) accept(level,maid,StepResult.SUCCESS);
             else accept(level,maid,StepResult.FAIL);
        } else accept(level,maid,StepResult.FAIL);
        BehaviorUtils.eraseTargetPos(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    public void accept(ServerLevel level, EntityMaid maid, StepResult result) {
        if (result == StepResult.SUCCESS) {
            ServeRequest request = Objects.requireNonNull((ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE));
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook) {
                ItemHandlerUtils.tryTakeFrom(
                        cooker.getAvailableInv(false),
                        maid.getAvailableInv(false),
                        StackPredicate.of(request.toServe.getItem()),
                        request.toServe.getCount()
                );
            }

            maid.swing(InteractionHand.OFF_HAND);
        }
    }
}
