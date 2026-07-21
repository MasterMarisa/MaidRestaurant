package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

public class IngredientUtil {
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
}
