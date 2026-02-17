package com.mastermarisa.maid_restaurant.storage;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TableStorage implements IMaidStorage {
    public static final String UID = "TableStorage";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(ModItems.TABLE_OAK.get()); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return getHandler(level,pos) != null;
    }

    @Override
    public @Nullable IItemHandler getHandler(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity table) {
            return table.getItems();
        }

        return null;
    }

    @Override
    public @NotNull ItemStack extract(Level level, BlockPos pos, int slot, int amount, boolean simulate) {
        IItemHandler handler = getHandler(level,pos);
        if (handler != null) {
            TableBlockEntity table = Objects.requireNonNull((TableBlockEntity) level.getBlockEntity(pos));
            ItemStack result = handler.extractItem(slot,amount,simulate);
            if (!simulate && !result.isEmpty()) {
                for (int i = slot + 1;i < handler.getSlots();i++) {
                    handler.insertItem(i - 1, handler.extractItem(i,1,false), false);
                }
                table.setChanged();
                table.refresh();
            }
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        IItemHandler handler = getHandler(level,pos);
        if (handler != null) {
            int index = -1;
            for (int i = 0;i < handler.getSlots();i++) {
                if (handler.getStackInSlot(i).isEmpty()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                TableBlockEntity table = Objects.requireNonNull((TableBlockEntity) level.getBlockEntity(pos));
                ItemStack result = handler.insertItem(index,stack.copy(),simulate);
                table.setChanged();
                table.refresh();
                return result;
            }
        }

        return stack;
    }
}
