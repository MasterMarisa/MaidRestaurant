package com.mastermarisa.maid_restaurant.init;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.mastermarisa.maid_restaurant.core.schedule.ChefInformation;

public interface ModTaskDataKeys {
    TaskDataKey<ChefInformation> CHEF_INFO = new ChefInformation.DataKey();

    static void register(TaskDataRegister register) {
        register.register(CHEF_INFO);
    }
}
