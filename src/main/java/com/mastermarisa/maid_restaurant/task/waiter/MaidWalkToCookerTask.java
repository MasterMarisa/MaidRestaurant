package com.mastermarisa.maid_restaurant.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.task.api.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidWalkToCookerTask extends MaidTickRateTask {
    private final double closeEnoughDist;

    public MaidWalkToCookerTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT),5,100);
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return BehaviorUtils.getType(maid) == TargetType.COOKER_POS.type && RequestManager.peekServeRequest(maid).isPresent();
    }

    @Override
    protected boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        return checkExtraStartConditions(level,maid) && RequestManager.peekServeRequest(maid).map(request -> {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
                return maid.distanceToSqr(cooker) > Math.pow(closeEnoughDist,2.0D);
            }
            return false;
        }).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level,maid,gameTime)) return;

        RequestManager.peekServeRequest(maid).ifPresent(request -> {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
                BehaviorUtils.setWalkAndLookTargetMemories(maid,cooker.blockPosition(),cooker.blockPosition(),0.4f,0);
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid entity, long gameTime) {
        if (BehaviorUtils.getType(entity) == TargetType.COOKER_POS.type) BehaviorUtils.eraseTargetPos(entity);
    }
}
