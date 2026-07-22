package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.behavior.TargetType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class MemoryUtil {
    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos walkPos, BlockPos lookPos, float pSpeed, int pDistance) {
        pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(walkPos, pSpeed, pDistance));
        pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(lookPos.above()));
        pLivingEntity.getBrain().setMemory(ModEntities.STAND_POS.get(), new BlockPosTracker(walkPos));
    }

    public static boolean isTarget(LivingEntity entity, TargetType type) {
        return entity.getBrain().getMemory(ModEntities.TARGET_TYPE.get()).map(t -> t == type.id).orElse(false);
    }

    public static void setTarget(LivingEntity entity, PositionTracker tracker, TargetType type){
        entity.getBrain().setMemory(ModEntities.TARGET_POS.get(),tracker);
        entity.getBrain().setMemory(ModEntities.TARGET_TYPE.get(),type.id);
    }

    public static void removeTarget(LivingEntity entity){
        entity.getBrain().eraseMemory(ModEntities.TARGET_POS.get());
        entity.getBrain().eraseMemory(ModEntities.TARGET_TYPE.get());
    }

    public static void removeTargetIfMatch(LivingEntity entity, TargetType type) {
        if (isTarget(entity, type)) {
            removeTarget(entity);
        }
    }

    public static <T> void setIfAbsent(LivingEntity entity, MemoryModuleType<T> moduleType, T value) {
        Brain<?> brain = entity.getBrain();
        if (brain.getMemory(moduleType).isEmpty()) {
            brain.setMemory(moduleType, value);
        }
    }
}
