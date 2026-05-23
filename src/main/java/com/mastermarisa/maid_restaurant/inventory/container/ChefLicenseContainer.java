package com.mastermarisa.maid_restaurant.inventory.container;

import com.mastermarisa.maid_restaurant.init.ModContainers;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.inventory.ZoneDefinitionHandler;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ChefLicenseContainer extends AbstractContainerMenu {
    public static final MenuType<ChefLicenseContainer> TYPE = IForgeMenuType.create(ChefLicenseContainer::new);

    private final ItemStack license;
    private final ZoneDefinitionHandler inventory;

    public ChefLicenseContainer(int id, Inventory playerInv, ItemStack license) {
        super(ModContainers.CHEF_LICENSE_CONTAINER.get(), id);
        this.license = license;
        this.inventory = ChefLicenseItem.getInventory(license);

        this.addSlot(new LicenseSlotHandler(inventory, 0, 26, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 1, 44, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 2, 62, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 3, 26, 70, license));
        this.addSlot(new LicenseSlotHandler(inventory, 4, 44, 70, license));
        this.addSlot(new LicenseSlotHandler(inventory, 5, 62, 70, license));

        this.addSlot(new LicenseSlotHandler(inventory, 6, 98, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 7, 116, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 8, 134, 52, license));
        this.addSlot(new LicenseSlotHandler(inventory, 9, 98, 70, license));
        this.addSlot(new LicenseSlotHandler(inventory, 10, 116, 70, license));
        this.addSlot(new LicenseSlotHandler(inventory, 11, 134, 70, license));

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInv, sj + (si + 1) * 9, 8 + sj * 18, 108 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInv, si, 8 + si * 18, 166));
    }

    public ChefLicenseContainer(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, extraData.readItem());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack stack1 = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot.hasItem()) {
            ItemStack stack2 = slot.getItem();
            stack1 = stack2.copy();
            if (i > 11) {
                if (!this.moveItemStackTo(stack2, 0, 12, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack2, 12, 48, true)) {
                return ItemStack.EMPTY;
            }
            if (stack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack1;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().is(ModItems.CHEF_LICENSE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            ChefLicenseItem.setInventory(license, inventory);
        }
    }

    private static class LicenseSlotHandler extends SlotItemHandler {
        private final ItemStack license;

        public LicenseSlotHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, ItemStack license) {
            super(itemHandler, index, xPosition, yPosition);
            this.license = license;
        }

        @Override
        public void setChanged() {
            ChefLicenseItem.setInventory(license, (ItemStackHandler) this.getItemHandler());
        }
    }
}
