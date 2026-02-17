package com.mastermarisa.maid_restaurant.utils;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BehaviorUtils {
    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos walkPos, BlockPos lookPos, float pSpeed, int pDistance) {
        setWalkTargetMemory(pLivingEntity,walkPos,pSpeed,pDistance);
        setLookTargetMemory(pLivingEntity,lookPos);
    }

    public static void setWalkTargetMemory(LivingEntity pLivingEntity, BlockPos walkPos, float pSpeed, int pDistance) {
        pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(walkPos, pSpeed, pDistance));
    }

    public static void setLookTargetMemory(LivingEntity pLivingEntity, BlockPos lookPos) {
        pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(lookPos.above()));
    }

    public static BlockPos getSearchPos(EntityMaid maid) {
        return maid.hasRestriction() ? maid.getRestrictCenter() : maid.blockPosition().below();
    }

    public static void setTargetPos(LivingEntity entity, PositionTracker tracker, int type){
        entity.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageManager.removeUser(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().setMemory(ModEntities.TARGET_POS.get(),tracker);
        entity.getBrain().setMemory(ModEntities.TARGET_TYPE.get(),type);
    }

    public static void eraseTargetPos(LivingEntity entity){
        entity.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageManager.removeUser(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().eraseMemory(ModEntities.TARGET_POS.get());
        entity.getBrain().eraseMemory(ModEntities.TARGET_TYPE.get());
    }

    public static int getTargetType(LivingEntity entity) {
        return entity.getBrain().getMemory(ModEntities.TARGET_TYPE.get()).orElse(-1);
    }
}
