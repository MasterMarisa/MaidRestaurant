package com.mastermarisa.maid_restaurant.init;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.mastermarisa.maid_restaurant.core.recipe.ContextList;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;

public interface ModTaskDataKeys {
    TaskDataKey<ContextList> CHEF_CONTEXTS = new ContextList.DATA_KEY();
    TaskDataKey<RestaurantZone> RESTAURANT_ZONE = new RestaurantZone.DATA_KEY();

    static void register(TaskDataRegister register) {
        register.register(CHEF_CONTEXTS);
        register.register(RESTAURANT_ZONE);
    }
}
