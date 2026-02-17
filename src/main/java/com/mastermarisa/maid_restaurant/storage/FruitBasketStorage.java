package com.mastermarisa.maid_restaurant.storage;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class FruitBasketStorage implements IMaidStorage {
    public static final String UID = "FruitBasketStorage";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(ModItems.FRUIT_BASKET.get()); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return getHandler(level,pos) != null;
    }

    @Override
    public @Nullable IItemHandler getHandler(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity basket) {
            return basket.getItems();
        }

        return null;
    }

    @Override
    public ItemStack extract(Level level, BlockPos pos, int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity basket) {
            ItemStack remainder = basket.getItems().extractItem(slot,amount,simulate);
            basket.refresh();
            return remainder;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity basket) {
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(basket.getItems(),stack,simulate);
            basket.refresh();
            return remainder;
        }

        return stack;
    }
}
