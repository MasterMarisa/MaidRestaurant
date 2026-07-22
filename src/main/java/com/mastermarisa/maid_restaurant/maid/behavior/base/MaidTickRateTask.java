package com.mastermarisa.maid_restaurant.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public abstract class MaidTickRateTask extends Behavior<EntityMaid> {
    protected int ticksRemain;

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int duration){
        super(entryCondition, duration);
    }

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition){
        super(entryCondition);
    }

    protected boolean shouldTick(ServerLevel level, EntityMaid maid, long gameTime){
        if (ticksRemain > 0) {
            ticksRemain--;
            return false;
        }
        ticksRemain = getInterval(level, maid);
        return true;
    }

    protected abstract int getInterval(ServerLevel level, EntityMaid maid);

    public void setTicksRemain(int ticksRemain) { this.ticksRemain = ticksRemain; }
}
