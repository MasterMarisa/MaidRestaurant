package com.mastermarisa.maid_restaurant.maid.task.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.api.IStep;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.maid.task.base.MaidTickRateTask;
import com.mastermarisa.maid_restaurant.maid.task.base.StepResult;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Objects;

public class MaidServeMealTask extends MaidTickRateTask implements IStep {
    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidServeMealTask(float movementSpeed, double closeEnoughDist) {
        super(ImmutableMap.of(ModEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT),5,60);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        if (request == null) return false;
        if (MaidStateManager.serveState(maid,level) != MaidStateManager.ServeState.SERVING) return false;
        if (request.targets.isEmpty()) return false;
        BlockPos pos = request.targets.getFirst().immutable();
        if (TaskWaiter.isValidServeBlock(level,pos) || request.targets.size() == 1) {
            BehaviorUtils.setTargetPos(maid,new BlockPosTracker(pos),5);
            BehaviorUtils.setWalkAndLookTargetMemories(maid,pos,pos,movementSpeed,0);
            return true;
        } else {
            BlockUsageManager.removeUser(request.targets.removeFirst(),maid.getUUID());
            return false;
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!shouldTick(level,maid,gameTime)) return;

        ServeRequest request = Objects.requireNonNull((ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE));
        BehaviorUtils.setWalkAndLookTargetMemories(maid,request.targets.getFirst(),request.targets.getFirst(),movementSpeed,0);
    }

    @Override
    protected boolean canStillUseCheck(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        return request != null && BehaviorUtils.getTargetType(maid) == 5 &&
                maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).map(tracker ->
                        maid.distanceToSqr(tracker.currentPosition()) > Math.pow(closeEnoughDist, 2.0D)
                ).orElse(false);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        if (request != null) {
            maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).ifPresentOrElse(tracker -> {
                if (maid.distanceToSqr(tracker.currentPosition()) <= Math.pow(closeEnoughDist, 2.0D)) accept(level,maid,StepResult.SUCCESS);
                else accept(level,maid,StepResult.FAIL);
            },() -> accept(level,maid,StepResult.FAIL));
        } else accept(level,maid,StepResult.FAIL);
        BehaviorUtils.eraseTargetPos(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    public void accept(ServerLevel level, EntityMaid maid, StepResult result) {
        if (result != StepResult.SUCCESS) return;
        BlockPos pos = maid.getBrain().getMemory(ModEntities.TARGET_POS.get()).get().currentBlockPosition();
        BlockState state = level.getBlockState(pos);
        ServeRequest request = Objects.requireNonNull((ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE));
        StackPredicate blockMeal = StackPredicate.of(s -> s.is(request.toServe.getItem()) && s.getItem() instanceof BlockItem);
        StackPredicate normalMeal = StackPredicate.of(request.toServe.getItem());

        if (!TaskWaiter.isValidServeBlock(level,pos)) {
            removeTargetTable(level,maid,pos,request);
            return;
        }

        if (state.is(TagBlock.SERVE_MEAL_BLOCK)) {
            if (level.getBlockEntity(pos) instanceof TableBlockEntity table) {
                ItemStackHandler tableItems = table.getItems();
                int count = getEmptySlots(tableItems);

                if (count == tableItems.getSlots()) {
                    ItemStack meal = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false), 1, blockMeal, false);
                    if (!meal.isEmpty()) {
                        serveBlockMeal(level,maid,request,pos,meal);
                    } else {
                        meal = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false), Math.min(request.toServe.getCount(),tableItems.getSlots()), normalMeal, false);
                        if (!meal.isEmpty()) {
                            serveMealItemToTable(level,maid,request,pos,table,meal);
                        }
                    }
                } else if (count > 0) {
                    ItemStack meal = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),Math.min(request.toServe.getCount(),count),normalMeal,false);
                    if (!meal.isEmpty())
                        serveMealItemToTable(level,maid,request,pos,table,meal);
                } else {
                    removeTargetTable(level,maid,pos,request);
                }
            } else {
                ItemStack meal = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false), 1, blockMeal, false);
                if (!meal.isEmpty())
                    serveBlockMeal(level,maid,request,pos,meal);
            }
        } else {
            IMaidStorage storage = MaidStorages.tryGetType(level,pos);
            if (storage == null) return;
            ItemStack remainder = request.toServe.copy();
            remainder = storage.insert(level,pos,remainder,true);
            ItemStack toInsert = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),request.toServe.getCount() - remainder.getCount(),normalMeal,false);
            request.toServe.split(toInsert.getCount());
            storage.insert(level,pos,toInsert,false);
            if (request.toServe.isEmpty()) {
                removeAllTargetTables(level,maid,request);
            } else {
                removeTargetTable(level,maid,pos,request);
            }
        }


        MaidRestaurant.LOGGER.debug("Count:" + request.toServe.getCount());
        MaidRestaurant.LOGGER.debug("Table:" + request.targets.size());
    }

    private static void serveBlockMeal(ServerLevel level, EntityMaid maid, ServeRequest request, BlockPos pos, ItemStack meal) {
        BlockPos maidPos = maid.blockPosition();
        Direction dir = DirectionUtils.getHorizontalDirection(pos.getX() - maidPos.getX(), pos.getZ() - maidPos.getZ());
        maid.placeItemBlock(InteractionHand.OFF_HAND, pos.above(), dir, meal);
        request.toServe.split(1);
        if (request.toServe.isEmpty()) {
            removeAllTargetTables(level,maid,request);
        } else {
            removeTargetTable(level,maid,pos,request);
        }
    }

    private static void serveMealItemToTable(ServerLevel level, EntityMaid maid, ServeRequest request, BlockPos pos, TableBlockEntity table, ItemStack meal) {
        ItemStackHandler tableItems = table.getItems();
        int tableIndex = tableItems.getSlots() - getEmptySlots(tableItems);

        while (tableIndex < tableItems.getSlots() && meal.getCount() > 0) {
            tableItems.setStackInSlot(tableIndex,meal.split(1));
            tableIndex++;
            request.toServe.split(1);
            table.refresh();
        }
        table.refresh();
        if (request.toServe.isEmpty()) {
            removeAllTargetTables(level,maid,request);
        } else {
            removeTargetTable(level,maid,pos,request);
        }
    }

    private static void removeTargetTable(ServerLevel level, EntityMaid maid, BlockPos pos, ServeRequest request) {
        BlockUsageManager.removeUser(request.targets.removeFirst(),maid.getUUID());
        if (request.targets.isEmpty()) {
            RequestManager.pop(maid,ServeRequest.TYPE);
            if (request.toServe.getCount() > 0) {
                ItemStack meal = ItemHandlerUtils.tryExtractSingleSlot(
                        maid.getAvailableInv(false),
                        request.toServe.getCount(),
                        StackPredicate.of(request.toServe.getItem()),
                        false
                );
                if (!meal.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5f, pos.getZ(), meal);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    private static void removeAllTargetTables(ServerLevel level, EntityMaid maid, ServeRequest request) {
        BlockPos pos = BlockPos.ZERO;
        for (var p : request.targets) {
            BlockUsageManager.removeUser(p,maid.getUUID());
            pos  = p;
        }

        request.targets.clear();
        RequestManager.pop(maid,ServeRequest.TYPE);
        if (request.toServe.getCount() > 0) {
            ItemStack meal = ItemHandlerUtils.tryExtractSingleSlot(
                    maid.getAvailableInv(false),
                    request.toServe.getCount(),
                    StackPredicate.of(request.toServe.getItem()),
                    false
            );
            if (!meal.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5f, pos.getZ(), meal);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }

    private static int getEmptySlots(ItemStackHandler itemStackHandler) {
        int count = 0;
        for (int i = 0;i < itemStackHandler.getSlots();i++)
            if (itemStackHandler.getStackInSlot(i).isEmpty())
                count++;

        return count;
    }
}
