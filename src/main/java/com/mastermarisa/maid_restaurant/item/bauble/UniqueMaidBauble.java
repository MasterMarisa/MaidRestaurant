package com.mastermarisa.maid_restaurant.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface UniqueMaidBauble extends IMaidBauble {
    @Override
    default void onPutOn(EntityMaid maid, ItemStack baubleItem) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getBaubleInSlot(i) instanceof ChefLicenseBauble
                    && maidBauble.getStackInSlot(i) != baubleItem) {
                Level level = maid.level();
                ItemStack toDrop = maidBauble.extractItem(i, 1, false);
                ItemEntity entity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), toDrop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }
    }
}
