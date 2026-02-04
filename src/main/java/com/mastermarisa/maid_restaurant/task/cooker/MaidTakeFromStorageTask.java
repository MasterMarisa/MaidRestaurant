package com.mastermarisa.maid_restaurant.task.cooker;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import com.mastermarisa.maid_restaurant.uitls.*;
import com.mastermarisa.maid_restaurant.uitls.manager.CheckRateManager;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import com.mastermarisa.maid_restaurant.uitls.manager.StateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class MaidTakeFromStorageTask extends Behavior<EntityMaid> {
    private final double closeEnoughDist;

    public MaidTakeFromStorageTask(double closeEnoughDist) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.closeEnoughDist = closeEnoughDist;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return BehaviorUtils.getType(maid) == TargetType.STORAGE_BLOCK.type && maid.distanceToSqr(BehaviorUtils.getTargetPos(maid).get().currentPosition()) <= Math.pow(closeEnoughDist,2.0D);
    }

    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        BlockPos pos = BehaviorUtils.getTargetPos(maid).get().currentBlockPosition();
        BlockState state = level.getBlockState(pos);

        BehaviorUtils.eraseTargetPos(maid);

        if (StateManager.cookState(maid) != StateManager.CookState.STORAGE) return;
        if (!state.is(TagBlock.STORAGE_BLOCK)) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, level.getBlockState(pos), blockEntity, null);
        if (handler == null) return;

        CookRequest request = RequestManager.peekCookRequest(maid).get();
        ICookTask iCookTask = RequestManager.getCurrentTask(maid).get();
        List<StackPredicate> required = iCookTask.getIngredients(RecipeUtils.byKeyTyped(request.type,request.id));
        for (StackPredicate predicate : required) {
            int count = MaidInvUtils.count(maid.getAvailableInv(false),predicate);
            if (count < 16) {
                MaidInvUtils.tryTakeFrom(handler,maid.getAvailableInv(false),predicate,16 - count);
            }
        }
        CheckRateManager.setNextCheckTick(maid.getUUID() + MaidSearchWorkBlockTask.UID,5);
        CheckRateManager.setNextCheckTick(maid.getUUID() + MaidSearchStorageTask.UID,5);
    }
}
