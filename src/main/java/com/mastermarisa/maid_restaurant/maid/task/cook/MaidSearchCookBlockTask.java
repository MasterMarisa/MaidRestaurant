package com.mastermarisa.maid_restaurant.maid.task.cook;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.BehaviorUtils;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.MaidStateManager;
import com.mastermarisa.maid_restaurant.utils.RequestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidSearchCookBlockTask extends MaidCheckRateTask {
    public static final String UID = "SearchWorkBlockRide";
    private final int verticalSearchRange;

    public MaidSearchCookBlockTask(EntityMaid maid, int maxCheckRate, int verticalSearchRange) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT), maid.getUUID() + "SearchWorkBlockRide", maxCheckRate, 60);
        this.verticalSearchRange = verticalSearchRange;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!maid.isPassenger()) return false;

        if (super.checkExtraStartConditions(level, maid)) {
            CookRequest request = (CookRequest) RequestManager.peek(maid, CookRequest.TYPE);
            if (request == null) return false;
            ICookTask iCookTask = CookTasks.getTask(request.type);
            BlockPos pos = iCookTask.searchWorkBlock(level, maid, (int)maid.getRestrictRadius(), this.verticalSearchRange);
            if (pos != null && pos.distSqr(maid.blockPosition()) <= 4 && MaidStateManager.cookState(maid, level) == MaidStateManager.CookState.COOK) {
                BehaviorUtils.setTargetPos(maid, new BlockPosTracker(pos), 2);
                maid.getBrain().setMemory(ModEntities.CHAIR_POS.get(), new BlockPosTracker(maid.blockPosition()));
                return true;
            }
        }
        return false;
    }
}
