package com.mastermarisa.maid_restaurant.maid.task.cook;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.IStep;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.maid.task.base.StepResult;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class MaidApproachCookBlockTask extends MaidCheckRateTask implements IStep {
    public static final String UID = "ApproachCookBlock";
    private final float movementSpeed;
    private final int verticalSearchRange;
    private final double closeEnoughDist;

    public MaidApproachCookBlockTask(EntityMaid maid, int maxCheckRate, float movementSpeed, int verticalSearchRange, double closeEnoughDist){
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),maid.getUUID() + UID, maxCheckRate,60);
        this.movementSpeed = movementSpeed;
        this.verticalSearchRange = verticalSearchRange;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return super.checkExtraStartConditions(level,maid) &&
                MaidStateManager.cookState(maid,level) == MaidStateManager.CookState.COOK &&
                search(level,maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.getTargetType(maid) == 1 &&
                maid.getBrain().getMemory(ModEntities.CHAIR_POS.get()).map(tracker ->
                        tracker.currentBlockPosition().distSqr(maid.blockPosition()) > Math.pow(closeEnoughDist,2.0D)
                ).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get().currentBlockPosition();
        BlockPos chair = maid.getBrain().getMemory(ModEntities.CHAIR_POS.get()).get().currentBlockPosition();
        BehaviorUtils.setWalkAndLookTargetMemories(maid,chair,pos,movementSpeed,0);
    }

    private boolean search(ServerLevel level, EntityMaid maid) {
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid,CookRequest.TYPE));
        ICookTask iCookTask = CookTasks.getTask(request.type);
        BlockPos pos = iCookTask.searchWorkBlock(level,maid,(int)maid.getRestrictRadius(),verticalSearchRange);
        if (pos != null && maid.isWithinRestriction(pos)) {
            List<BlockPos> possible = getPossibleChairPos(pos);
            for (BlockPos chair : possible){
                if (level.getBlockState(chair).is(TagBlock.SIT_BLOCK) && isChairAvailable(level,maid,chair)){
                    maid.getBrain().setMemory(ModEntities.CHAIR_POS.get(),new BlockPosTracker(chair));
                    BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),1);
                    BehaviorUtils.setWalkAndLookTargetMemories(maid,chair,pos,movementSpeed,0);
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos getRelativeBelow(BlockPos pos, Direction dir){
        return pos.below().relative(dir);
    }

    private List<BlockPos> getPossibleChairPos(BlockPos pos) {
        return List.of(
                getRelativeBelow(pos, Direction.NORTH),
                getRelativeBelow(pos,Direction.SOUTH),
                getRelativeBelow(pos,Direction.EAST),
                getRelativeBelow(pos,Direction.WEST)
        );
    }

    private boolean isChairAvailable(ServerLevel level, EntityMaid maid, BlockPos chair) {
        List<EntityMaid> maids = level.getEntitiesOfClass(EntityMaid.class,new AABB(chair));
        return maids.isEmpty() || (maids.size() == 1 && maids.getFirst().equals(maid));
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (maid.getBrain().hasMemoryValue(ModEntities.TARGET_POS.get())) {
            maid.getBrain().getMemory(ModEntities.CHAIR_POS.get()).ifPresentOrElse(tracker -> {
                BlockPos pos = tracker.currentBlockPosition();
                if (pos.distSqr(maid.blockPosition()) <= Math.pow(closeEnoughDist,2.0D)) accept(level,maid,StepResult.SUCCESS);
                else accept(level,maid,StepResult.FAIL);
            },() -> accept(level,maid,StepResult.FAIL));
        } else accept(level,maid,StepResult.FAIL);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    public void accept(ServerLevel level, EntityMaid maid, StepResult result) {
        if (result == StepResult.FAIL || MaidStateManager.cookState(maid,level) != MaidStateManager.CookState.COOK) {
            BehaviorUtils.eraseTargetPos(maid);
            maid.getBrain().eraseMemory(ModEntities.CHAIR_POS.get());
        } else {
            maid.getBrain().getMemory(ModEntities.CHAIR_POS.get()).ifPresent(tracker -> {
                BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get().currentBlockPosition();
                Vec3 chair = tracker.currentPosition();
                chair = chair.relative(DirectionUtils.getHorizontalDirection(pos.getX() - chair.x,pos.getZ() - chair.z),0.2f);
                maid.teleportTo(chair.x,chair.y,chair.z);
                maid.setDeltaMovement(Vec3.ZERO);
                BehaviorUtils.setLookTargetMemory(maid,pos);
                maid.getBrain().setMemory(ModEntities.TARGET_TYPE.get(),2);
            });
        }
    }
}
