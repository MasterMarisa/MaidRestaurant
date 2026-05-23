package com.mastermarisa.maid_restaurant.init;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.mastermarisa.maid_restaurant.core.schedule.ChefInfo;

public interface ModTaskDataKeys {
    TaskDataKey<ChefInfo> CHEF_INFO = new ChefInfo.DataKey();

    static void register(TaskDataRegister register) {
        register.register(CHEF_INFO);
    }
}
