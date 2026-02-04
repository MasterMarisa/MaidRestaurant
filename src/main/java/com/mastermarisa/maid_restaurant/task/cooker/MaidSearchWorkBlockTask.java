package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.api.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import com.mastermarisa.maid_restaurant.uitls.manager.StateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;

public class MaidSearchWorkBlockTask extends MaidCheckRateTask {
    public static final String UID = "SearchWorkBlock";
    private final float movementSpeed;
    private final int verticalSearchRange;

    public MaidSearchWorkBlockTask(EntityMaid maid, int maxCheckRate, float movementSpeed, int verticalSearchRange){
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),maid.getUUID().toString() + UID, maxCheckRate,60);
        this.movementSpeed = movementSpeed;
        this.verticalSearchRange = verticalSearchRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return super.checkExtraStartConditions(level,maid) && RequestManager.getCurrentTask(maid).map(iCookTask -> {
            BlockPos pos = iCookTask.searchWorkBlock(level,maid,(int)maid.getRestrictRadius(),verticalSearchRange);
            if (pos != null && maid.isWithinRestriction(pos) && StateManager.cookState(maid) == StateManager.CookState.COOK) {
                BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),TargetType.COOK_BLOCK);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        BlockPos pos = BehaviorUtils.getTargetPos(maid).get().currentBlockPosition();

        List<BlockPos> possible = List.of(getRelativeBelow(pos, Direction.NORTH),getRelativeBelow(pos,Direction.SOUTH),getRelativeBelow(pos,Direction.EAST),getRelativeBelow(pos,Direction.WEST));

        for (BlockPos chair : possible){
            if (level.getBlockState(chair).is(TagBlock.SIT_BLOCK) && !BehaviorUtils.isRiding(level,pos)){
                BehaviorUtils.setChairPos(maid,new BlockPosTracker(chair));
                BehaviorUtils.setWalkAndLookTargetMemories(maid,chair,pos,movementSpeed,0);
                return;
            }
        }

        BehaviorUtils.eraseTargetPos(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.getType(maid) == TargetType.COOK_BLOCK.type;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!maid.isPassenger() && BehaviorUtils.getType(maid) == TargetType.COOK_BLOCK.type) {
            BehaviorUtils.eraseTargetPos(maid);
        }
    }

    private BlockPos getRelativeBelow(BlockPos pos, Direction dir){
        return pos.below().relative(dir);
    }
}
