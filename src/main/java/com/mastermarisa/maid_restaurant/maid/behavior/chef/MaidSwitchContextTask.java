package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.maid.behavior.base.MaidCheckRateTask;
import net.minecraft.server.level.ServerLevel;

public class MaidSwitchContextTask extends MaidCheckRateTask {
    public static final String UID = "SwitchContext";

    public MaidSwitchContextTask(int maxInterval) {
        super(ImmutableMap.of(), maxInterval);
    }

    @Override
    public String getUID() { return UID; }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return ChefScheduler.getContextList(maid).getCurrentIndex() == -1
                && super.checkExtraStartConditions(level, maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ChefScheduler.checkUnblock(level, maid);
        ChefScheduler.switchToNextContext(maid);
    }
}
