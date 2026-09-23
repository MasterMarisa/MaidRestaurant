package com.mastermarisa.maid_restaurant.data.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class OrderEntry implements INBTSerializable<CompoundTag> {
    private static final String TAG_COUNT = "count";
    private static final String TAG_MENU_ENTRY = "menu_entry";

    private int count;
    private MenuEntry entry;

    private OrderEntry() {}

    public OrderEntry(int count, MenuEntry entry) {
        this.count = count;
        this.entry = entry;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setEntry(MenuEntry entry) {
        this.entry = entry;
    }

    public int getCount() {
        return this.count;
    }

    public MenuEntry getEntry() {
        return this.entry;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_COUNT, this.count);
        tag.put(TAG_MENU_ENTRY, this.entry.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_COUNT)) {
            this.count = tag.getInt(TAG_COUNT);
        }
        if (tag.contains(TAG_MENU_ENTRY)) {
            this.entry = MenuEntry.fromNBT(tag.getCompound(TAG_MENU_ENTRY));
        }
    }

    public static OrderEntry fromNBT(CompoundTag tag) {
        OrderEntry orderEntry = new OrderEntry();
        orderEntry.deserializeNBT(tag);
        return orderEntry;
    }
}