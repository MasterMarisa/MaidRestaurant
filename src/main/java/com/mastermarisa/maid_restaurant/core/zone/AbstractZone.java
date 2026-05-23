package com.mastermarisa.maid_restaurant.core.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractZone implements Iterable<BlockPos>, INBTSerializable<CompoundTag> {
    public abstract boolean contains(BlockPos pos);
}
