package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    public static int findStackSlot(IItemHandler handler, Ingredient ingredient, int start, int end) {
        for(int i = start; i < Math.min(handler.getSlots(), end); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) return i;
        }
        return -1;
    }

    public static int findStackSlot(IItemHandler handler, Ingredient ingredient) {
        return findStackSlot(handler, ingredient, 0, handler.getSlots());
    }

    public static List<Integer> findStackSlots(IItemHandler handler, Ingredient ingredient, int start, int end) {
        IntList slots = new IntArrayList();
        for(int i = start; i < Math.min(handler.getSlots(), end); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) slots.add(i);
        }
        return slots;
    }

    public static List<Integer> findStackSlots(IItemHandler handler, Ingredient ingredient) {
        return findStackSlots(handler, ingredient, 0, handler.getSlots());
    }

    public static boolean isStackIn(IItemHandler handler, Ingredient ingredient) {
        return findStackSlot(handler, ingredient) != -1;
    }

    public static int count(IItemHandler handler, Ingredient ingredient) {
        int count = 0;
        for (int i = 0;i < handler.getSlots();i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static List<ItemStack> tryExtract(IItemHandler handler, int count, Ingredient ingredient, boolean strict, boolean simulate) {
        List<Integer> slots = findStackSlots(handler, ingredient);
        List<ItemStack> stacks = new ArrayList<>();
        if (!strict || count <= count(handler, ingredient)) {
            for (int slot : slots) {
                ItemStack itemStack = handler.extractItem(slot, count, simulate);
                stacks.add(itemStack);
                count -= itemStack.getCount();
                if (count <= 0) break;
            }
        }
        return stacks;
    }

    public static boolean tryTake(ServerLevel level, BlockPos pos, IMaidStorage from, IItemHandler to, Ingredient ingredient, int count) {
        if (!from.isValid(level, pos)) return false;
        List<ItemStack> itemStacks = from.extract(level, pos, ingredient, count, false);
        for (var stack : itemStacks) {
            int pre = stack.getCount();
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(to, stack, false);
            count -= pre - leftover.getCount();
            if (!leftover.isEmpty()) {
                from.insert(level, pos, leftover, false);
            }
        }

        return count <= 0;
    }

    public static boolean equals(Ingredient a, Ingredient b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.isEmpty() && b.isEmpty();
        }

        if (a instanceof StrictNBTIngredient != b instanceof StrictNBTIngredient) {
            return false;
        }

        if (a instanceof PartialNBTIngredient != b instanceof PartialNBTIngredient) {
            return false;
        }

        ItemStack[] stacksA = a.getItems();
        ItemStack[] stacksB = b.getItems();
        if (stacksA.length != stacksB.length) {
            return false;
        }

        boolean[] matchedB = new boolean[stacksB.length];
        for (ItemStack stackA : stacksA) {
            boolean found = false;
            for (int i = 0; i < stacksB.length; i++) {
                if (!matchedB[i] && ItemStack.isSameItemSameTags(stackA, stacksB[i])) {
                    matchedB[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
