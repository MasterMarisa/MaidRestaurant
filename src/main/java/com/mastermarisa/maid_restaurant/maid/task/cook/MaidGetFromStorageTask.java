package com.mastermarisa.maid_restaurant.maid.task.cook;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.api.IStep;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidCheckRateTask;
import com.mastermarisa.maid_restaurant.maid.task.base.StepResult;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

public class MaidGetFromStorageTask extends MaidCheckRateTask implements IStep {
    public static final String UID = "GetFromStorage";
    private final float movementSpeed;
    private final int verticalSearchRange;
    private final double closeEnoughDist;

    public MaidGetFromStorageTask(EntityMaid maid, int maxCheckRate, float movementSpeed, int verticalSearchRange, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),maid.getUUID() + UID,maxCheckRate,60);
        this.movementSpeed = movementSpeed;
        this.verticalSearchRange = verticalSearchRange;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid){
        return super.checkExtraStartConditions(level,maid) &&
                MaidStateManager.cookState(maid,level) == MaidStateManager.CookState.STORAGE &&
                search(level,maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return BehaviorUtils.getTargetType(maid) == 0 &&
                maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
                    tracker.currentBlockPosition().distSqr(maid.blockPosition()) > Math.pow(closeEnoughDist,2.0D)
                ).orElse(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresent(tracker -> {
            BehaviorUtils.setWalkAndLookTargetMemories(maid,tracker.currentBlockPosition(),tracker.currentBlockPosition(),movementSpeed,0);
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresentOrElse(tracker -> {
            BlockPos pos = tracker.currentBlockPosition();
            if (pos.distSqr(maid.blockPosition()) <= Math.pow(closeEnoughDist,2.0D)) accept(level,maid,StepResult.SUCCESS);
            else accept(level,maid,StepResult.FAIL);
        },() -> accept(level,maid,StepResult.FAIL));
        if (BehaviorUtils.getTargetType(maid) == 0) BehaviorUtils.eraseTargetPos(maid);
    }

    protected boolean search(ServerLevel level, EntityMaid maid) {
        BlockPos center = BehaviorUtils.getSearchPos(maid);
        int searchRange = (int)maid.getRestrictRadius();
        List<BlockPos> foundStorages = SearchUtils.search(center,searchRange,verticalSearchRange,(pos)->{
            IItemHandler handler = MaidStorages.tryGetHandler(level,pos);
            return handler != null && containsRequired(level,maid,handler)
                    && !SearchUtils.getAllRelativeGround(level,pos,1).stream().filter(maid::canPathReach).toList().isEmpty();
        });

        return foundStorages.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).map(pos->{
            BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),0);
            BehaviorUtils.setWalkAndLookTargetMemories(maid,pos,pos,movementSpeed,0);
            return true;
        }).orElse(false);
    }

    protected boolean containsRequired(ServerLevel level, EntityMaid maid, IItemHandler itemHandler) {
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid,CookRequest.TYPE));
        ICookTask iCookTask = CookTasks.getTask(request.type);
        List<StackPredicate> required = iCookTask.getIngredients(level.getRecipeManager().byKey(request.id).get());
        List<ItemStack> handler = ItemHandlerUtils.toStacks(maid.getAvailableInv(false));

        Optional<PositionTracker> cached = maid.getBrain().getMemory(ModEntities.CACHED_WORK_BLOCK.get());
        if (cached.isPresent()) {
            BlockPos pos = cached.get().currentBlockPosition();
            if (BlockUsageManager.getUserCount(pos) <= 0 || BlockUsageManager.isUsing(pos,maid.getUUID()))
                handler.addAll(iCookTask.getCurrentInput(maid.level(),pos,maid));
        }

        List<Pair<StackPredicate,Integer>> filtered;
        if (iCookTask.getType().equals(ModRecipes.STOCKPOT_RECIPE))
            filtered = ItemHandlerUtils.filterByCountStockpot(required,handler,request.remain);
        else
            filtered = ItemHandlerUtils.filterByCount(required,handler,request.remain);
        return filtered.stream().anyMatch(s -> ItemHandlerUtils.isStackIn(itemHandler,s.left()));
    }

    @Override
    public void accept(ServerLevel level, EntityMaid maid, StepResult result) {
        if (result == StepResult.FAIL) return;

        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get().currentBlockPosition();

        BehaviorUtils.eraseTargetPos(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        if (MaidStateManager.cookState(maid,level) != MaidStateManager.CookState.STORAGE) return;
        IMaidStorage storage = MaidStorages.tryGetType(level,pos);
        if (storage == null) return;

        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid,CookRequest.TYPE));
        ICookTask iCookTask = CookTasks.getTask(request.type);
        List<StackPredicate> required = iCookTask.getIngredients(level.getRecipeManager().byKey(request.id).get());
        List<ItemStack> stacks = ItemHandlerUtils.toStacks(maid.getAvailableInv(false));

        Optional<PositionTracker> cached = maid.getBrain().getMemory(ModEntities.CACHED_WORK_BLOCK.get());
        if (cached.isPresent()) {
            BlockPos p = cached.get().currentBlockPosition();
            if (BlockUsageManager.getUserCount(p) <= 0 || BlockUsageManager.isUsing(p,maid.getUUID()))
                stacks.addAll(iCookTask.getCurrentInput(maid.level(),p,maid));
        }

        List<Pair<StackPredicate,Integer>> filtered;
        if (iCookTask.getType().equals(ModRecipes.STOCKPOT_RECIPE))
            filtered = ItemHandlerUtils.filterByCountStockpot(required,stacks,request.remain);
        else
            filtered = ItemHandlerUtils.filterByCount(required,stacks,request.remain);
        for (var pair : filtered)
            ItemHandlerUtils.tryTakeFrom(level,pos,storage,maid.getAvailableInv(false),pair.left(),pair.right());

        maid.swing(InteractionHand.OFF_HAND);

        CheckRateManager.setNextCheckTick(maid.getUUID() + UID,5);
        CheckRateManager.setNextCheckTick(maid.getUUID() + MaidApproachCookBlockTask.UID,5);
    }
}
