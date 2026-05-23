package com.mastermarisa.maid_restaurant.inventory;

import com.mastermarisa.maid_restaurant.item.CuboidZoneDefinitionItem;
import com.mastermarisa.maid_restaurant.item.PointSetZoneDefinitionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ZoneDefinitionHandler extends ItemStackHandler {
    public ZoneDefinitionHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.getItem() instanceof PointSetZoneDefinitionItem
                || stack.getItem() instanceof CuboidZoneDefinitionItem;
    }
}
