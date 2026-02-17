package com.mastermarisa.maid_restaurant.api.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IRequest extends INBTSerializable<CompoundTag> {
    boolean checkEnableConditions(ServerLevel level, EntityMaid maid);
}
