package com.mastermarisa.maid_restaurant.uitls;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;

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
}
