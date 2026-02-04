package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.SitEntity;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

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

    public static Optional<PositionTracker> getTargetPos(LivingEntity entity){
        return entity.getBrain().getMemory(InitEntities.TARGET_POS.get());
    }

    public static void setTargetPos(LivingEntity entity, PositionTracker tracker, int type){
        entity.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageManager.removeUser(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().setMemory(InitEntities.TARGET_POS.get(),tracker);
        entity.getBrain().setMemory(InitEntities.TARGET_TYPE.get(),type);
    }

    public static void setTargetPos(LivingEntity entity, PositionTracker tracker, TargetType type){
        setTargetPos(entity,tracker,type.type);
    }

    public static void eraseTargetPos(LivingEntity entity){
        entity.getBrain().getMemory(InitEntities.TARGET_POS.get()).ifPresent(p->{
            BlockUsageManager.removeUser(p.currentBlockPosition(),entity.getUUID());
        });
        entity.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        entity.getBrain().eraseMemory(InitEntities.TARGET_TYPE.get());
    }

    public static int getType(LivingEntity entity) {
        return entity.getBrain().getMemory(InitEntities.TARGET_TYPE.get()).orElse(-1);
    }

    public static Optional<PositionTracker> getChairPos(LivingEntity entity){
        return entity.getBrain().getMemory(InitEntities.CHAIR_POS.get());
    }

    public static void setChairPos(LivingEntity entity, PositionTracker tracker){
        entity.getBrain().setMemory(InitEntities.CHAIR_POS.get(),tracker);
    }

    public static void eraseChairPos(LivingEntity entity){
        entity.getBrain().eraseMemory(InitEntities.CHAIR_POS.get());
    }

    public static void startRide(ServerLevel level, EntityMaid maid, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        if (state.is(TagBlock.SIT_BLOCK)){
            List<SitEntity> entities = level.getEntitiesOfClass(SitEntity.class, new AABB(pos));
            if (entities.isEmpty()) {
                SitEntity entitySit = new SitEntity(level, pos);
                entitySit.setYRot(((Direction)state.getValue(HorizontalDirectionalBlock.FACING)).toYRot());
                level.addFreshEntity(entitySit);
                maid.startRiding(entitySit, true);
            }
        }
    }

    public static void startRide(Level level, EntityMaid maid, BlockPos pos, Direction dir){
        BlockState state = level.getBlockState(pos);
        if (state.is(TagBlock.SIT_BLOCK)){
            List<SitEntity> entities = level.getEntitiesOfClass(SitEntity.class, new AABB(pos));
            if (entities.isEmpty()) {
                SitEntity entitySit = new SitEntity(level, pos);
                entitySit.setYRot((dir).toYRot());
                level.addFreshEntity(entitySit);
                maid.startRiding(entitySit, true);
            }
        }
    }

    public static void stopRide(Level level, EntityMaid maid){
        maid.stopRiding();
        List<SitEntity> entities = level.getEntitiesOfClass(SitEntity.class, new AABB(maid.blockPosition()));
        for (SitEntity entity : entities){
            entity.discard();
        }
    }

    public static boolean isRiding(ServerLevel level,BlockPos pos){
        BlockState state = level.getBlockState(pos);
        if (state.is(TagBlock.SIT_BLOCK)){
            List<SitEntity> entities = level.getEntitiesOfClass(SitEntity.class, new AABB(pos));
            return !entities.isEmpty();
        }
        return false;
    }
}
