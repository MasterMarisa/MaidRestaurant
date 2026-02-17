package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.maid.task.base.StepResult;
import net.minecraft.server.level.ServerLevel;

public interface IStep {
    void accept(ServerLevel level, EntityMaid maid, StepResult result);
}
