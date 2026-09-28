package com.mastermarisa.maid_restaurant.core.plan;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.world.WorldContext;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class NodePlanner {
    private NodePlanner() {}

    public static Resolution resolve(ExecutionNode node, PlanInput input) {
        return resolve(node, input, requiredCount(node, input));
    }

    public static Resolution resolve(ExecutionNode node, PlanInput input, int required) {
        if (required <= 0) return new Resolution.Satisfied();

        Need need = new Need(node.getIngredient(), required);

        Resolution fromSupply = fetch(need, input.supply());
        if (fromSupply != null) return fromSupply;

        WorldContext world = input.world();
        if (hasRecipe(node, world) && !node.getChildren().isEmpty()) {
            List<Need> children = childNeeds(node, need.amount(), world, input.childrenInPlace());
            return new Resolution.Craft(need, children, isReady(children));
        }

        return new Resolution.Impossible(need,
                node.isLeaf() ? Resolution.Reason.NO_SUPPLY : Resolution.Reason.NO_RECIPE);
    }

    public static int requiredCount(ExecutionNode node, PlanInput input) {
        return requiredCount(node, input.world(), input.parentCount(), input.inPlace());
    }

    private static int requiredCount(ExecutionNode node, WorldContext world, int parentCount, List<ItemStack> inPlace) {
        int required = RecipeNode.calculateCountByParent(world, node.getRecipeNode(), parentCount);
        if (required > 0 && !inPlace.isEmpty()) {
            required -= InvUtil.count(inPlace, node.getIngredient());
        }
        return Math.max(0, required);
    }

    private static List<Need> childNeeds(ExecutionNode node, int parentCount, WorldContext world, List<ItemStack> inPlace) {
        List<Need> needs = new ArrayList<>(node.getChildren().size());
        for (ExecutionNode child : node.getChildren()) {
            needs.add(new Need(child.getIngredient(), requiredCount(child, world, parentCount, inPlace)));
        }
        return List.copyOf(needs);
    }

    private static boolean isReady(List<Need> children) {
        for (Need child : children) {
            if (child.amount() > 0) return false;
        }
        return true;
    }

    @Nullable
    private static Resolution fetch(Need need, @Nullable ISupplySource supply) {
        if (supply == null) return null;

        SupplyTarget target = supply.findFirst(need.ingredient());
        if (target == null) return null;
        return new Resolution.Fetch(need, target);
    }

    private static boolean hasRecipe(ExecutionNode node, WorldContext world) {
        return node.getCapability() != null && node.getRecipeNode().getRecipe(world.recipes()) != null;
    }
}
