package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class InvUtil {
    public static int findSlot(IItemHandler handler, Ingredient ingredient, int start, int end) {
        for(int i = start; i < Math.min(handler.getSlots(), end); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) return i;
        }
        return -1;
    }

    public static int findSlot(IItemHandler handler, Ingredient ingredient) {
        return findSlot(handler, ingredient, 0, handler.getSlots());
    }

    public static List<Integer> findSlots(IItemHandler handler, Ingredient ingredient, int start, int end) {
        IntList slots = new IntArrayList();
        for(int i = start; i < Math.min(handler.getSlots(), end); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) slots.add(i);
        }
        return slots;
    }

    public static List<Integer> findSlots(IItemHandler handler, Ingredient ingredient) {
        return findSlots(handler, ingredient, 0, handler.getSlots());
    }

    public static boolean isStackIn(IItemHandler handler, Ingredient ingredient) {
        return findSlot(handler, ingredient) != -1;
    }

    public static int count(IItemHandler handler, Ingredient ingredient) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean contains(IItemHandler handler, Ingredient ingredient, int count) {
        if (count == 1) {
            return isStackIn(handler, ingredient);
        }
        return count(handler, ingredient) >= count;
    }

    public static boolean contains(List<ItemStack> items, Ingredient ingredient, int count) {
        int sum = 0;
        for (ItemStack stack : items) {
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum > count) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(IItemHandler handler, List<ItemStack> itemStacks, Ingredient ingredient, int count) {
        int sum = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum >= count) {
                    return true;
                }
            }
        }
        for (ItemStack stack : itemStacks) {
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<ItemStack> tryExtract(IItemHandler handler, int count, Ingredient ingredient, boolean strict) {
        return tryExtract(handler, count, ingredient, strict, false);
    }

    public static List<ItemStack> tryExtract(IItemHandler handler, int count, Ingredient ingredient, boolean strict, boolean simulate) {
        List<Integer> slots = findSlots(handler, ingredient);
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
            ItemStack restItem = ItemHandlerHelper.insertItemStacked(to, stack, false);
            count -= pre - restItem.getCount();
            if (!restItem.isEmpty()) {
                from.insert(level, pos, restItem, false);
            }
        }

        return count <= 0;
    }

    public static void getItemToMaid(EntityMaid maid, ItemStack stack) {
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false), stack, false);
        if (!remainder.isEmpty()) {
            ItemEntity dropItem = maid.spawnAtLocation(stack);
            if (dropItem != null) {
                dropItem.setPickUpDelay(0);
            }
        }
    }

    public static void getAllFromInv(Inventory inventory, EntityMaid maid) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                getItemToMaid(maid, itemStack);
            }
        }
        inventory.clearContent();
    }

    public static void exchangeToHand(EntityMaid maid, InteractionHand hand, int index) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack remainder = maidInv.extractItem(index, 64, false);
        ItemStack itemInHand = maid.getItemInHand(hand).copyAndClear();
        if (!remainder.isEmpty()) {
            maid.setItemInHand(hand, remainder);
        }
        if (!itemInHand.isEmpty()) {
            maidInv.insertItem(index, itemInHand, false);
        }
    }
}
