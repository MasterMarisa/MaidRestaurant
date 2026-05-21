package com.mastermarisa.maid_restaurant.maid.init;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.item.bauble.ChefLicenseBauble;
import com.mastermarisa.maid_restaurant.maid.task.TaskChef;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskChef());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new MaidBrain());
    }

    @Override
    public void registerTaskData(TaskDataRegister register) {
        ModTaskDataKeys.register(register);
    }

    @Override
    public void bindMaidBauble(BaubleManager manager) {
        manager.bind(ModItems.CHEF_LICENSE.get(), new ChefLicenseBauble());
    }
}
