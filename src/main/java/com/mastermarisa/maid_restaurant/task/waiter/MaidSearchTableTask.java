package com.mastermarisa.maid_restaurant.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequest;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.uitls.BehaviorUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.TargetType;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MaidSearchTableTask extends Behavior<EntityMaid> {
    private final float movementSpeed;

    public MaidSearchTableTask(float movementSpeed) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),30);
        this.movementSpeed = movementSpeed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return RequestManager.peekServeRequest(maid).isPresent();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        ServeRequest request = RequestManager.peekServeRequest(maid).get();
        if (MaidInvUtils.count(maid.getAvailableInv(false), StackPredicate.of(request.toServe.getItem())) < request.toServe.getCount()) {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
                BehaviorUtils.setTargetPos(maid,new BlockPosTracker(cooker.blockPosition()),TargetType.COOKER_POS);
                BehaviorUtils.eraseChairPos(maid);
            }
            return;
        }

        if (!request.targetTables.isEmpty()) {
            BlockPos pos = request.targetTables.getFirst().immutable();
            if (!(level.getBlockState(pos).is(TagBlock.SERVE_MEAL_BLOCK) && level.getBlockState(pos.above()).canBeReplaced())) {
                if (request.targetTables.size() == 1) {
                    BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos), TargetType.DROP_MEAL_POS);
                    BehaviorUtils.eraseChairPos(maid);
                    BehaviorUtils.setWalkAndLookTargetMemories(maid,pos,pos,movementSpeed,0);
                } else {
                    request.targetTables.removeFirst();
                }
                return;
            }
            BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),TargetType.SERVE_TABLE_POS);
            BehaviorUtils.eraseChairPos(maid);
            BehaviorUtils.setWalkAndLookTargetMemories(maid,pos,pos,movementSpeed,0);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.getType(maid) == TargetType.DROP_MEAL_POS.type || BehaviorUtils.getType(maid) == TargetType.SERVE_TABLE_POS.type;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        if (BehaviorUtils.getType(maid) == TargetType.DROP_MEAL_POS.type || BehaviorUtils.getType(maid) == TargetType.SERVE_TABLE_POS.type) BehaviorUtils.eraseTargetPos(maid);
    }
}
