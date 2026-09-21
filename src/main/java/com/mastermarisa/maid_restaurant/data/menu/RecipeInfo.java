package com.mastermarisa.maid_restaurant.data.menu;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record RecipeInfo(ItemStack output, List<Ingredient> inputs, List<ItemStack> workBlocks) {
    public static RecipeInfo fromNode(RecipeNode root) {
        ItemStack output = root.getIngredient().getItems()[0];
        List<Ingredient> inputs = new ArrayList<>();
        List<ItemStack> workBlocks = new ArrayList<>();
        traversalLeaf(root, node -> {
            Ingredient ingredient = node.getIngredient();
            boolean contained = inputs.stream().anyMatch(i -> IngredientUtil.equals(i, ingredient));
            if (!contained) {
                inputs.add(ingredient);
            }
        });
        traversalStep(root, step -> {
            ICookCapability capability = step.getCapability();
            if (capability != null) {
                ItemStack icon = capability.getIcon();
                boolean contained = workBlocks.stream().anyMatch(s -> ItemStack.isSameItem(s, icon));
                if (!contained) {
                    workBlocks.add(icon);
                }
            }
        });
        return new RecipeInfo(output, inputs, workBlocks);
    }

    private static void traversalStep(RecipeNode node, Consumer<RecipeStep> consumer) {
        if (node.isLeaf()) {
            return;
        }
        consumer.accept(node.getStep());
        for (RecipeNode child : node.getChildren()) {
            traversalStep(child, consumer);
        }
    }

    private static void traversalLeaf(RecipeNode node, Consumer<RecipeNode> consumer) {
        if (node.isLeaf()) {
            consumer.accept(node);
            return;
        }
        for (RecipeNode child : node.getChildren()) {
            traversalLeaf(child, consumer);
        }
    }
}
