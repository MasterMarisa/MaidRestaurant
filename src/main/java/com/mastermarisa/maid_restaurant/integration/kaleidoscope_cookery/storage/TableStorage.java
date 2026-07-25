package com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.storage;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class TableStorage implements IMaidStorage {
    public static final ResourceLocation ID = new ResourceLocation("kaleidoscope_cookery", "table");

    public static void register() {
        StorageRegistry.register(new TableStorage());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return ModItems.TABLE_OAK.get().getDefaultInstance(); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TableBlockEntity && level.getBlockState(pos.above()).canBeReplaced();
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            List<ItemStack> result = new ArrayList<>();
            ItemStackHandler handler = be.getItems();
            for (int i = 0; i < handler.getSlots() && amount > 0; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    amount--;
                    result.add(handler.extractItem(i, 1, false));
                }
            }
            for (int j = 1; j < handler.getSlots(); j++) {
                for (int i = 0; i < handler.getSlots() - j; i++) {
                    ItemStack pre = handler.getStackInSlot(i);
                    ItemStack post = handler.getStackInSlot(i + 1);
                    if (pre.isEmpty() && !post.isEmpty()) {
                        handler.setStackInSlot(i, handler.extractItem(i + 1, 1, false));
                    }
                }
            }
            if (!result.isEmpty()) {
                be.refresh();
            }
            return result;
        }
        return List.of();
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            ItemStack remainder = stack.copy();
            ItemStackHandler handler = be.getItems();
            for (int i = 0; i < handler.getSlots() && !remainder.isEmpty(); i++) {
                if (handler.getStackInSlot(i).isEmpty()) {
                    ItemStack toInsert = remainder.split(1);
                    handler.insertItem(i, toInsert, simulate);
                }
            }
            if (remainder.getCount() != stack.getCount()) {
                be.refresh();
            }
            return remainder;
        }
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            return InvUtil.count(be.getItems(), ingredient);
        }
        return 0;
    }
}
