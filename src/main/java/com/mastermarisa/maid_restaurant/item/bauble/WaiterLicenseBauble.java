package com.mastermarisa.maid_restaurant.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;

public class WaiterLicenseBauble implements UniqueMaidBauble {
    @Override
    public boolean syncClient(EntityMaid maid, ItemStack baubleItem) {
        return true;
    }
}
