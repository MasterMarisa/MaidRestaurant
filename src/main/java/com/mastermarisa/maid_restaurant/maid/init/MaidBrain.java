package com.mastermarisa.maid_restaurant.maid.init;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.init.ModEntities;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class MaidBrain implements IExtraMaidBrain {
    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return Lists.newArrayList(
                ModEntities.TARGET_POS.get(),
                ModEntities.TARGET_TYPE.get(),
                ModEntities.STAND_POS.get()
        );
    }
}
