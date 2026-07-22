package com.mastermarisa.maid_restaurant.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public abstract class MaidCheckRateTask extends Behavior<EntityMaid> {
    protected final int maxInterval;

    public MaidCheckRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxInterval, int duration) {
        super(entryCondition, duration);
        this.maxInterval = maxInterval;
    }

    public MaidCheckRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxInterval) {
        super(entryCondition);
        this.maxInterval = maxInterval;
    }

    public abstract String getUID();

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return CheckRateHelper.shouldCheck(maid.getUUID(), getUID(), maid.getRandom(), maxInterval);
    }
}
