package com.mastermarisa.maid_restaurant.utils;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ItemHandlerUtils {
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
            if (filter.test(stack)) return i;
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
        return getRequired(required,toStacks(handler));
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

    public static void tryTakeFrom(ServerLevel level, BlockPos pos, IMaidStorage iStorageType, IItemHandler to, StackPredicate predicate, int count) {
        if (!iStorageType.isValid(level,pos)) return;
        IItemHandler handler = iStorageType.getHandler(level,pos);
        if (handler == null) return;
        for (int i = 0;i < handler.getSlots() && count > 0;i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (predicate.test(stack)) {
                if (stack.getCount() > count) {
                    ItemStack remainder = stack.copyWithCount(count);
                    remainder = ItemHandlerHelper.insertItemStacked(to,remainder,true);
                    ItemStack toInsert = iStorageType.extract(level,pos,i,count - remainder.getCount(),false);
                    ItemHandlerHelper.insertItemStacked(to,toInsert,false);
                    break;
                } else {
                    ItemStack remainder = stack.copyWithCount(count);
                    remainder = ItemHandlerHelper.insertItemStacked(to,remainder,true);
                    ItemStack toInsert = iStorageType.extract(level,pos,i,stack.getCount() - remainder.getCount(),false);
                    count -= toInsert.getCount() - ItemHandlerHelper.insertItemStacked(to,toInsert,false).getCount();
                }
            }
        }
    }

    public static List<ItemStack> toStacks(IItemHandler itemHandler) {
        List<ItemStack> ans = new ArrayList<>();
        for (int i = 0;i < itemHandler.getSlots();i++)
            if (!itemHandler.getStackInSlot(i).isEmpty())
                ans.add(itemHandler.getStackInSlot(i));

        return ans;
    }

    public static List<Pair<StackPredicate,Integer>> filterByCount(List<StackPredicate> required, List<ItemStack> handler, int count) {
        LinkedList<ItemStack> stacks = new LinkedList<>();
        for (var stack : handler)
            if (!stack.isEmpty())
                stacks.add(stack.copy());

        int[] remain = new int[required.size()];
        Arrays.fill(remain, count);

        while (!stacks.isEmpty()) {
            ItemStack stack = stacks.removeFirst();
            for (int i = 0;i < required.size();i++) {
                if (remain[i] > 0 && required.get(i).test(stack)) {
                    if (remain[i] <= stack.getCount()) {
                        stack.split(remain[i]);
                        remain[i] = 0;
                    } else {
                        remain[i] -= stack.getCount();
                        stack.split(stack.getCount());
                    }
                }
            }
        }

        List<Pair<StackPredicate,Integer>> ans = new ArrayList<>();
        for (int i = 0;i < required.size();i++)
            if (remain[i] > 0)
                ans.add(Pair.of(required.get(i),remain[i]));

        return ans;
    }

    public static List<Pair<StackPredicate,Integer>> filterByCountStockpot(List<StackPredicate> required, List<ItemStack> handler, int count) {
        LinkedList<ItemStack> stacks = new LinkedList<>();
        for (var stack : handler)
            if (!stack.isEmpty())
                stacks.add(stack.copy());

        int[] remain = new int[required.size()];
        Arrays.fill(remain, count);
        for (int i = 0;i < required.size();i++)
            if (required.get(i).test(new ItemStack(Items.WATER_BUCKET)))
                remain[i] = 2;

        while (!stacks.isEmpty()) {
            ItemStack stack = stacks.removeFirst();
            for (int i = 0;i < required.size();i++) {
                if (remain[i] > 0 && required.get(i).test(stack)) {
                    if (remain[i] <= stack.getCount()) {
                        stack.split(remain[i]);
                        remain[i] = 0;
                    } else {
                        remain[i] -= stack.getCount();
                        stack.split(stack.getCount());
                    }
                }
            }
        }

        List<Pair<StackPredicate,Integer>> ans = new ArrayList<>();
        for (int i = 0;i < required.size();i++)
            if (remain[i] > 0)
                ans.add(Pair.of(required.get(i),remain[i]));

        return ans;
    }
}
