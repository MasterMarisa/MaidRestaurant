package com.mastermarisa.maid_restaurant.core.zone;

import net.minecraft.core.BlockPos;

public abstract class AbstractZone implements Iterable<BlockPos> {
    public abstract boolean contains(BlockPos pos);
}
