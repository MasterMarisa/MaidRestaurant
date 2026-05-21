package com.mastermarisa.maid_restaurant.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChefLicenseBauble implements IMaidBauble {
    @Override
    public void onPutOn(EntityMaid maid, ItemStack baubleItem) {
        BaubleItemHandler handler = maid.getMaidBauble();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getBaubleInSlot(i) instanceof ChefLicenseBauble
                && handler.getStackInSlot(i) != baubleItem) {
                Level level = maid.level();
                ItemStack toDrop = handler.extractItem(i, 1, false);
                ItemEntity entity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), toDrop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }
    }

    @Override
    public boolean syncClient(EntityMaid maid, ItemStack baubleItem) {
        return true;
    }
}
