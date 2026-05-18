package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BehaviorUtils {
    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos walkPos, BlockPos lookPos, float pSpeed, int pDistance) {
        pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(walkPos, pSpeed, pDistance));
        pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(lookPos.above()));
    }

    public static void setTarget(LivingEntity entity, PositionTracker tracker, TargetType type){
        entity.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageUtils.remove(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().setMemory(ModEntities.TARGET_POS.get(),tracker);
        entity.getBrain().setMemory(ModEntities.TARGET_TYPE.get(),type.id);
    }

    public static void eraseTarget(LivingEntity entity){
        entity.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageUtils.remove(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().eraseMemory(ModEntities.TARGET_POS.get());
        entity.getBrain().eraseMemory(ModEntities.TARGET_TYPE.get());
    }

    public static boolean isTarget(LivingEntity entity, TargetType type) {
        return entity.getBrain().getMemory(ModEntities.TARGET_TYPE.get()).map(t -> t.equals(type.id)).orElse(false);
    }
}
