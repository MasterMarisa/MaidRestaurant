package com.mastermarisa.maid_restaurant;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.mastermarisa.maid_restaurant.entity.ExtraMaidBrain;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.task.TaskWaiter;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager){
        manager.add(new TaskCooker());
        manager.add(new TaskWaiter());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new ExtraMaidBrain());
    }
}
