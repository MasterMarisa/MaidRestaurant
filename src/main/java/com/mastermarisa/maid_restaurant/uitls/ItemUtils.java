package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.LinkedList;
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

    public static List<ItemStack> toList(IItemHandler handler) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        return itemStacks;
    }

    public static boolean allMatch(List<Ingredient> ingredients, List<ItemStack> itemStacks) {
        LinkedList<ItemStack> available = new LinkedList<>();
        for (var stack : itemStacks) {
            if (!stack.isEmpty()) {
                available.add(stack);
            }
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;
            boolean matched = false;
            for (int j = 0; j < available.size(); j++) {
                ItemStack stack = available.get(j);
                if (ingredient.test(stack)) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        available.remove(j);
                    }
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(List<Ingredient> ingredients, IItemHandler handler) {
        return allMatch(ingredients, toList(handler));
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

    public static boolean areIngredientsEqual(Ingredient a, Ingredient b) {
        if (!a.isVanilla() || !b.isVanilla()) {
            return false;
        }
        List<TagKey<Item>> tagKeysA = new ArrayList<>();
        List<TagKey<Item>> tagKeysB = new ArrayList<>();
        List<ItemStack> itemsA = new ArrayList<>();
        List<ItemStack> itemsB = new ArrayList<>();

        for (Ingredient.Value value : a.values) {
            if (value instanceof Ingredient.TagValue tagValue) {
                tagKeysA.add(tagValue.tag);
            } else if (value instanceof Ingredient.ItemValue itemValue) {
                itemsA.add(itemValue.item);
            } else {
                return false;
            }
        }

        for (Ingredient.Value value : b.values) {
            if (value instanceof Ingredient.TagValue tagValue) {
                tagKeysB.add(tagValue.tag);
            } else if (value instanceof Ingredient.ItemValue itemValue) {
                itemsB.add(itemValue.item);
            } else {
                return false;
            }
        }

        if (tagKeysA.size() != tagKeysB.size() || itemsA.size() != itemsB.size()) {
            return false;
        }

        for (TagKey<Item> keyA : tagKeysA) {
            boolean matched = false;
            for (TagKey<Item> keyB : tagKeysB) {
                if (keyA.equals(keyB)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        for (ItemStack stackA : itemsA) {
            boolean matched = false;
            for (ItemStack stackB : itemsB) {
                if (stackA.is(stackB.getItem())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return true;
    }
}
