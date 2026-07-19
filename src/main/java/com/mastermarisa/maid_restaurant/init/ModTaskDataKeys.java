package com.mastermarisa.maid_restaurant.init;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.mastermarisa.maid_restaurant.core.schedule.ChefInformation;
import com.mastermarisa.maid_restaurant.core.schedule.WorkBlockCache;

public interface ModTaskDataKeys {
    TaskDataKey<ChefInformation> CHEF_INFO = new ChefInformation.DataKey();
    TaskDataKey<WorkBlockCache> WORK_BLOCK_CACHE = new WorkBlockCache.DataKey();

    static void register(TaskDataRegister register) {
        register.register(CHEF_INFO);
        register.register(WORK_BLOCK_CACHE);
    }
}
