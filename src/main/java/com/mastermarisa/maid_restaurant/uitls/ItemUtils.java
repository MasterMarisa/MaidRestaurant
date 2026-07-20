package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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

    public static boolean contains(IItemHandler handler, Ingredient ingredient, int count) {
        if (count == 1) {
            return isStackIn(handler, ingredient);
        }
        return count(handler, ingredient) >= count;
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
        if (a == b) {
            return true;
        }

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

    public static void getItemToLivingEntity(LivingEntity entity, ItemStack stack) {
        getItemToLivingEntity(entity, stack, -1);
    }

    public static void getItemToLivingEntity(LivingEntity entity, ItemStack stack, int preferredSlot) {
        if (!stack.isEmpty()) {
            if (entity.getMainHandItem().isEmpty()) {
                RandomSource random = entity.level().random;
                entity.setItemInHand(InteractionHand.MAIN_HAND, stack);
                entity.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            } else if (entity instanceof Player player) {
                ItemHandlerHelper.giveItemToPlayer(player, stack, preferredSlot);
            } else {
                ItemEntity dropItem = entity.spawnAtLocation(stack);
                if (dropItem != null) {
                    dropItem.setPickUpDelay(0);
                }
            }
        }
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
}
