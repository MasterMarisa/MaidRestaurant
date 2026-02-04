package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.api.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import com.mastermarisa.maid_restaurant.uitls.manager.StateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidSearchWorkBlockTaskRide extends MaidCheckRateTask {
    public static final String UID = "SearchWorkBlockRide";
    private final int verticalSearchRange;

    public MaidSearchWorkBlockTaskRide(EntityMaid maid, int maxCheckRate, int verticalSearchRange){
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),maid.getUUID().toString() + UID, maxCheckRate,60);
        this.verticalSearchRange = verticalSearchRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return super.checkExtraStartConditions(level,maid) && maid.isPassenger() && RequestManager.getCurrentTask(maid).map(iCookTask -> {
            BlockPos pos = iCookTask.searchWorkBlock(level,maid,(int)maid.getRestrictRadius(),verticalSearchRange);
            if (pos != null && maid.isWithinRestriction(pos) && StateManager.cookState(maid) == StateManager.CookState.COOK) {
                BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos), TargetType.COOK_BLOCK);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
