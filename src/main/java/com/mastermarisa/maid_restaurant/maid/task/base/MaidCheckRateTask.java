package com.mastermarisa.maid_restaurant.maid.task.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.utils.CheckRateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class MaidCheckRateTask extends Behavior<EntityMaid> {
    protected final String key;
    protected final int maxCheckRate;

    public MaidCheckRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, String key, int maxCheckRate, int duration) {
        super(entryCondition,duration);
        this.key = key;
        this.maxCheckRate = maxCheckRate;
    }

    public MaidCheckRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, String key, int maxCheckRate) {
        super(entryCondition);
        this.key = key;
        this.maxCheckRate = maxCheckRate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return CheckRateManager.check(key,maxCheckRate,maid.getRandom());
    }
}
