package com.mastermarisa.maid_restaurant.uitls.manager;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;

import java.util.List;

public class StateManager {
    public static CookState cookState(EntityMaid maid) {
        return RequestManager.peekCookRequest(maid).map(request ->
            CookTaskManager.getTask(request.type).map(iCookTask -> {
                List<StackPredicate> required = iCookTask.getIngredients(RecipeUtils.byKeyTyped(iCookTask.getType(),request.id));
                required.addAll(iCookTask.getKitchenWares());
                return MaidInvUtils.getRequired(required,maid.getAvailableInv(false)).isEmpty() ? CookState.COOK : CookState.STORAGE;
            }).orElse(CookState.IDLE)
        ).orElse(CookState.IDLE);
    }

    public static enum CookState {
        IDLE(0),
        STORAGE(1),
        COOK(2);

        private CookState(int state) { this.state = state; }
        private final int state;

        public int getState() {
            return state;
        }
    }
}
