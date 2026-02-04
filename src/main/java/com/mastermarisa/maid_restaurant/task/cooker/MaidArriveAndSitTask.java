package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.DirectionUtils;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;

public class MaidArriveAndSitTask extends Behavior<EntityMaid> {
    private final double closeEnoughDist;

    public MaidArriveAndSitTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.CHAIR_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return BehaviorUtils.getChairPos(maid).map((targetPos) ->
                BehaviorUtils.getType(maid) == TargetType.COOK_BLOCK.type && maid.distanceToSqr(targetPos.currentPosition()) <= Math.pow(closeEnoughDist,2.0D)
        ).orElse(false);
    }

    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        BehaviorUtils.getChairPos(maid).ifPresent((tracker)->{
            BlockPos pos = tracker.currentBlockPosition();
            BlockState state = level.getBlockState(pos);
            if (state.is(TagBlock.SIT_BLOCK)){
                BehaviorUtils.getTargetPos(maid).ifPresentOrElse((posTracker)->{
                    BlockPos tar = posTracker.currentBlockPosition();
                    BehaviorUtils.startRide(level,maid,pos, DirectionUtils.getHorizontalDirection(tar.getX()-pos.getX(),tar.getZ()-pos.getZ()));
                },()->{
                    BehaviorUtils.startRide(level,maid,pos);
                });
            }
        });
    }
}
