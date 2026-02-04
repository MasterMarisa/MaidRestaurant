package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.task.api.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.uitls.*;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import com.mastermarisa.maid_restaurant.uitls.manager.StateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaidSearchStorageTask extends MaidCheckRateTask {
    public static final String UID = "SearchStorage";
    private final float movementSpeed;
    private final int verticalSearchRange;

    public MaidSearchStorageTask(EntityMaid maid, int maxCheckRate, float movementSpeed, int verticalSearchRange) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),maid.getUUID().toString() + UID,maxCheckRate,60);
        this.movementSpeed = movementSpeed;
        this.verticalSearchRange = verticalSearchRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid){
        return super.checkExtraStartConditions(level,maid) && StateManager.cookState(maid) == StateManager.CookState.STORAGE
                && search(level,maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.getType(maid) == TargetType.STORAGE_BLOCK.type;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid entity, long gameTime) {
        if (BehaviorUtils.getType(entity) == TargetType.STORAGE_BLOCK.type) BehaviorUtils.eraseTargetPos(entity);
    }

    protected boolean search(ServerLevel level, EntityMaid maid) {
        BlockPos center = BehaviorUtils.getSearchPos(maid);
        int searchRange = (int)maid.getRestrictRadius();
        List<BlockPos> foundStorages = BlockPosUtils.search(center,searchRange,verticalSearchRange,(pos)->{
            BlockState state = level.getBlockState(pos);
            if (state.is(TagBlock.STORAGE_BLOCK)){
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null){
                    IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, level.getBlockState(pos), blockEntity, null);
                    return handler != null && containsRequired(maid,handler)
                            && !BlockPosUtils.getAllRelativeGround(level,pos,1).stream().filter(maid::canPathReach).toList().isEmpty();
                }
            }
            return false;
        });

        return foundStorages.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).map(pos->{
            List<BlockPos> available = new ArrayList<>(BlockPosUtils.getAllRelativeGround(level,pos,2));
            Collections.shuffle(available);
            BlockPos walkTar = available.getFirst();
            BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),TargetType.STORAGE_BLOCK);
            BehaviorUtils.eraseChairPos(maid);
            BehaviorUtils.setWalkAndLookTargetMemories(maid,walkTar,pos,movementSpeed,0);
            return true;
        }).orElse(false);
    }

    protected boolean containsRequired(EntityMaid maid, IItemHandler itemHandler) {
        ICookTask iCookTask = RequestManager.getCurrentTask(maid).get();
        CookRequest request = RequestManager.peekCookRequest(maid).get();
        List<StackPredicate> required = iCookTask.getIngredients(RecipeUtils.byKeyTyped(request.type,request.id));
        return required.stream().filter(s-> MaidInvUtils.count(maid.getAvailableInv(false),s) < 16).anyMatch(s->MaidInvUtils.isStackIn(itemHandler,s));
    }
}
