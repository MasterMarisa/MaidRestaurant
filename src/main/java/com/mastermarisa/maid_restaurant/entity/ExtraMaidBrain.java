package com.mastermarisa.maid_restaurant.entity;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.mastermarisa.maid_restaurant.init.InitEntities;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.ArrayList;
import java.util.List;

public class ExtraMaidBrain implements IExtraMaidBrain {
    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return new ArrayList<>(List.of(InitEntities.TARGET_POS.get(),InitEntities.CHAIR_POS.get(),InitEntities.TARGET_TYPE.get()));
    }
}
