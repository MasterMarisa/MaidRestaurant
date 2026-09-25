package com.mastermarisa.maid_restaurant.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.data.task_data.ChefInformation;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.data.zone.CombinedZoneWrapper;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.item.ZoneDefinitionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class ChefLicenseBauble implements UniqueMaidBauble {
    @Override
    public void onPutOn(EntityMaid maid, ItemStack baubleItem) {
        UniqueMaidBauble.super.onPutOn(maid, baubleItem);

        // 读取并解析厨师执照定义的工作区与储存区，并缓存到女仆身上
        ItemStackHandler handler = ChefLicenseItem.getInventory(baubleItem);
        List<AbstractZone> workZones = new ArrayList<>();
        List<AbstractZone> storageZones = new ArrayList<>();
        List<AbstractZone> prepZones = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            if (itemStack.getItem() instanceof ZoneDefinitionItem) {
                AbstractZone zone = ZoneDefinitionItem.getZone(itemStack);
                if (zone == null) {
                    continue;
                }
                if (i < 4) {
                    workZones.add(zone);
                } else if (i < 8) {
                    storageZones.add(zone);
                } else {
                    prepZones.add(zone);
                }
            }
        }
        CombinedZoneWrapper workZoneWrapper = workZones.isEmpty() ? null : new CombinedZoneWrapper(workZones);
        CombinedZoneWrapper storageZoneWrapper = storageZones.isEmpty() ? null : new CombinedZoneWrapper(storageZones);
        CombinedZoneWrapper prepZoneWrapper = prepZones.isEmpty() ? null : new CombinedZoneWrapper(prepZones);
        ChefInformation chefInfo = new ChefInformation(workZoneWrapper, storageZoneWrapper, prepZoneWrapper);
        maid.setData(ModTaskDataKeys.CHEF_INFO, chefInfo);
    }

    @Override
    public boolean syncClient(EntityMaid maid, ItemStack baubleItem) {
        return true;
    }
}
