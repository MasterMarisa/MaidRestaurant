package com.mastermarisa.maid_restaurant.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public abstract class MaidTickRateTask extends Behavior<EntityMaid> {
    protected final int maxInterval;
    protected int ticksRemain;

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxInterval, int duration){
        super(entryCondition,duration);
        this.maxInterval = maxInterval;
    }

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxInterval){
        super(entryCondition);
        this.maxInterval = maxInterval;
    }

    protected boolean shouldTick(ServerLevel level, EntityMaid maid, long gameTime){
        if (ticksRemain > 0) {
            ticksRemain--;
            return false;
        }
        int halfInterval = maxInterval / 2;
        int offset = maxInterval % 2 == 0 ? 1 : 2;
        ticksRemain = halfInterval + maid.getRandom().nextInt(halfInterval + offset);
        return true;
    }

    public void setTicksRemain(int ticksRemain) { this.ticksRemain = ticksRemain; }
}
