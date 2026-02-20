package com.mastermarisa.maid_restaurant.utils;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class MaidStateManager {
    public static CookState cookState(EntityMaid maid, Level level) {
        CookRequest request = (CookRequest) RequestManager.peek(maid,CookRequest.TYPE);
        if (request == null || !request.checkEnableConditions((ServerLevel) level,maid)) return CookState.IDLE;

        ICookTask iCookTask = CookTasks.getTask(request.type);
        List<StackPredicate> required = iCookTask.getIngredients(level.getRecipeManager().byKey(request.id).get(),level);
        required.addAll(iCookTask.getKitchenWares());
        List<ItemStack> handler = ItemHandlerUtils.toStacks(maid.getAvailableInv(false));
        Optional<PositionTracker> cached = maid.getBrain().getMemory(ModEntities.CACHED_WORK_BLOCK.get());
        if (cached.isPresent()) {
            BlockPos pos = cached.get().currentBlockPosition();
            if (BlockUsageManager.getUserCount(pos) <= 0 || BlockUsageManager.isUsing(pos,maid.getUUID()))
                handler.addAll(iCookTask.getCurrentInput(maid.level(),pos,maid));
        }
        return ItemHandlerUtils.getRequired(required,handler).isEmpty() ? CookState.COOK : CookState.STORAGE;
    }

    public static ServeState serveState(EntityMaid maid, ServerLevel level) {
        ServeRequest request = (ServeRequest) RequestManager.peek(maid,ServeRequest.TYPE);
        if (request == null || !request.checkEnableConditions(level,maid)) return ServeState.IDLE;

        if (ItemHandlerUtils.count(maid.getAvailableInv(false), StackPredicate.of(request.toServe.getItem())) < request.toServe.getCount()) {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCook)
                return ServeState.TAKING;
            else return ServeState.IDLE;
        } else return ServeState.SERVING;
    }

    public static enum CookState {
        IDLE,
        STORAGE,
        COOK;
    }

    public static enum ServeState {
        IDLE,
        TAKING,
        SERVING
    }
}
