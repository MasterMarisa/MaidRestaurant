package com.mastermarisa.maid_restaurant.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.mastermarisa.maid_restaurant.core.schedule.ChefInfo;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.core.zone.CombinedZoneWrapper;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.item.ZoneDefinitionItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class ChefLicenseBauble implements IMaidBauble {
    @Override
    public void onPutOn(EntityMaid maid, ItemStack baubleItem) {
        // 只允许同时装备一个
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

        // 读取并解析厨师执照定义的工作区与储存区，并缓存到女仆身上
        ItemStackHandler handler = ChefLicenseItem.getInventory(baubleItem);
        List<AbstractZone> workZones = new ArrayList<>();
        List<AbstractZone> storageZones = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            if (itemStack.getItem() instanceof ZoneDefinitionItem) {
                AbstractZone zone = ZoneDefinitionItem.getZone(itemStack);
                if (i < 6) {
                    workZones.add(zone);
                } else {
                    storageZones.add(zone);
                }
            }
        }
        CombinedZoneWrapper workZoneWrapper = workZones.isEmpty() ? null : new CombinedZoneWrapper(workZones);
        CombinedZoneWrapper storageZoneWrapper = storageZones.isEmpty() ? null : new CombinedZoneWrapper(storageZones);
        ChefInfo chefInfo = new ChefInfo(workZoneWrapper, storageZoneWrapper);
        maid.setData(ModTaskDataKeys.CHEF_INFO, chefInfo);
    }

    @Override
    public boolean syncClient(EntityMaid maid, ItemStack baubleItem) {
        return true;
    }
}
