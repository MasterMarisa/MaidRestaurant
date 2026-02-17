package com.mastermarisa.maid_restaurant.maid.task.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public abstract class MaidTickRateTask extends Behavior<EntityMaid> {
    protected final int maxTickRate;
    protected int nextTickCount;

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxTickRate){
        super(entryCondition);
        this.maxTickRate = maxTickRate;
    }

    public MaidTickRateTask(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int maxTickRate, int duration){
        super(entryCondition,duration);
        this.maxTickRate = maxTickRate;
    }

    protected boolean shouldTick(ServerLevel level, EntityMaid maid, long gameTime){
        if (nextTickCount > 0) {
            nextTickCount--;
            return false;
        }
        nextTickCount = maxTickRate + maid.getRandom().nextInt(maxTickRate);
        return true;
    }

    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        if (nextTickCount > 0){
            return true;
        } else {
            return canStillUseCheck(level,maid,gameTimeIn);
        }
    }

    public void setNextTickCount(int nextTickCount) { this.nextTickCount = nextTickCount; }

    protected abstract boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn);
}
