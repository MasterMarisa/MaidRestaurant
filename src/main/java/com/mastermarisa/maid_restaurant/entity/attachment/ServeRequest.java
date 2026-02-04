package com.mastermarisa.maid_restaurant.entity.attachment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.UUID;

public class ServeRequest {
    public ItemStack toServe;
    public LinkedList<BlockPos> targetTables;
    public UUID provider;
    public int requestedCount;

    public ServeRequest() {}

    public ServeRequest(ItemStack toServe, long[] targetTables, UUID provider) {
        this.toServe = toServe;
        this.targetTables = loadPos(targetTables);
        this.provider = provider;
        this.requestedCount = toServe.getCount();
    }

    public ServeRequest(ItemStack toServe, long[] targetTables, UUID provider, int requestedCount) {
        this.toServe = toServe;
        this.targetTables = loadPos(targetTables);
        this.provider = provider;
        this.requestedCount = requestedCount;
    }

    private LinkedList<BlockPos> loadPos(long[] packed) {
        LinkedList<BlockPos> posList = new LinkedList<>();
        for (long pack : packed) {
            posList.add(new BlockPos(BlockPos.getX(pack),BlockPos.getY(pack),BlockPos.getZ(pack)));
        }
        return posList;
    }
}
