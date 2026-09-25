package com.mastermarisa.maid_restaurant.inventory.container;

import com.mastermarisa.maid_restaurant.init.ModContainers;
import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class WaiterLicenseContainer extends AbstractContainerMenu {
    public static final MenuType<WaiterLicenseContainer> TYPE = IForgeMenuType.create(WaiterLicenseContainer::new);

    private final ItemStack itemStack;

    public WaiterLicenseContainer(int id, Inventory playerInv, ItemStack itemStack) {
        super(ModContainers.WAITER_LICENSE_CONTAINER.get(), id);
        this.itemStack = itemStack;
    }

    public WaiterLicenseContainer(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, extraData.readItem());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().is(ModItems.WAITER_LICENSE.get());
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
