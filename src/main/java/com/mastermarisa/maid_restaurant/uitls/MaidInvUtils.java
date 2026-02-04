package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaidInvUtils {
    public static ItemStack tryGet(IItemHandler handler, StackPredicate filter) {
        int slotIndex = findStackSlot(handler, filter);
        return slotIndex >= 0 ? handler.getStackInSlot(slotIndex) : ItemStack.EMPTY;
    }

    public static List<ItemStack> tryExtract(IItemHandler handler, int count, StackPredicate filter, boolean strict) {
        List<Integer> slots = findStackSlots(handler,filter);
        List<ItemStack> stacks = new ArrayList<>();
        int c = count(handler,filter);
        if (!strict || c >= count) {
            c = 0;
            for (int i : slots) {
                if (c >= count) break;
                ItemStack stack = handler.getStackInSlot(i);
                if (count - c >= stack.getCount()) {
                    c += stack.getCount();
                    stacks.add(handler.extractItem(i,stack.getCount(),false));
                } else {
                    stacks.add(handler.extractItem(i,count-c,false));
                    c = count;
                }
            }
            return stacks;
        }

        return new ArrayList<>();
    }

    public static ItemStack tryExtractSingleSlot(IItemHandler handler, int count, StackPredicate filter, boolean strict) {
        for (int i = 0;i < handler.getSlots();i++){
            ItemStack stack = handler.getStackInSlot(i);
            if (filter.test(stack) && (stack.getCount() >= count || !strict)){
                return handler.extractItem(i,count,false);
            }
        }

        return ItemStack.EMPTY;
    }

    public static int findStackSlot(IItemHandler handler, StackPredicate filter) {
        for(int i = 0; i < handler.getSlots(); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (filter.test(stack)) {
                return i;
            }
        }

        return -1;
    }

    public static List<Integer> findStackSlots(IItemHandler handler, StackPredicate filter) {
        IntList slots = new IntArrayList();

        for(int i = 0; i < handler.getSlots(); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (filter.test(stack)) {
                slots.add(i);
            }
        }

        return slots;
    }

    public static boolean isStackIn(IItemHandler handler, StackPredicate filter) {
        return findStackSlot(handler, filter) >= 0;
    }

    public static boolean isStackIn(EntityMaid maid, StackPredicate filter) {
        return findStackSlot(maid.getAvailableInv(false), filter) >= 0;
    }

    public static boolean canItemInsert(IItemHandler inventory, ItemStack testStack) {
        for(int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack remainder = inventory.insertItem(i, testStack, true);
            if (remainder.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public static int count(IItemHandler handler, StackPredicate predicate) {
        int count = 0;
        for (int i = 0;i < handler.getSlots();i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public static int count(List<ItemStack> itemStacks) {
        int count = 0;
        for (ItemStack stack : itemStacks) {
            count += stack.getCount();
        }

        return count;
    }

    public static List<StackPredicate> getRequired(List<StackPredicate> required, IItemHandler handler) {
        List<StackPredicate> remain = new ArrayList<>(required);
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0;i < handler.getSlots();i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            for (int j = 0;j < itemStack.getCount();j++)
                stacks.add(itemStack.copyWithCount(1));
        }

        for (ItemStack itemStack : stacks)
            for (int i = 0;i < remain.size();i++)
                if (remain.get(i).test(itemStack)){
                    remain.remove(remain.get(i));
                    break;
                }

        return remain;
    }

    public static List<StackPredicate> getRequired(List<StackPredicate> required, List<ItemStack> slots) {
        List<StackPredicate> remain = new ArrayList<>(required);
        List<ItemStack> stacks = new ArrayList<>();

        for (ItemStack itemStack : slots)
            for (int i = 0;i < itemStack.getCount();i++)
                stacks.add(itemStack.copyWithCount(1));

        for (ItemStack itemStack : stacks)
            for (int i = 0;i < remain.size();i++)
                if (remain.get(i).test(itemStack)){
                    remain.remove(remain.get(i));
                    break;
                }

        return remain;
    }

    public static void tryTakeFrom(IItemHandler from, IItemHandler to, StackPredicate predicate, int count) {
        for (int i = 0;i < from.getSlots() && count > 0;i++){
            ItemStack stack = from.getStackInSlot(i);
            if (predicate.test(stack)){
                if (stack.getCount() > count){
                    ItemStack toInsert = stack.copyWithCount(count);
                    toInsert = ItemHandlerHelper.insertItemStacked(to,toInsert,false);
                    stack.split(count - toInsert.getCount());
                    break;
                } else {
                    ItemStack toInsert = stack.copyWithCount(stack.getCount());
                    toInsert = ItemHandlerHelper.insertItemStacked(to,toInsert,false);
                    count -= stack.getCount() - toInsert.getCount();
                    stack.split(stack.getCount() - toInsert.getCount());
                }
            }
        }
    }
}
