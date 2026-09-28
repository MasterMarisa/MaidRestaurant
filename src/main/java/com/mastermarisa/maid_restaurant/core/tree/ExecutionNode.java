package com.mastermarisa.maid_restaurant.core.tree;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.plan.Resolution;
import com.mastermarisa.maid_restaurant.core.world.IRecipeLookup;
import com.mastermarisa.maid_restaurant.core.world.WorldContext;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExecutionNode {
    private final RecipeNode recipeNode;
    private Progress progress;
    @Nullable
    private Resolution resolution;
    @Nullable
    private ExecutionNode parent;
    private final List<ExecutionNode> children;

    public ExecutionNode(RecipeNode recipeNode) {
        this.recipeNode = recipeNode;
        this.progress = Progress.PENDING;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public static ExecutionNode fromRecipeTree(RecipeNode root) {
        return buildTree(root, null);
    }

    private static ExecutionNode buildTree(RecipeNode recipeNode, @Nullable ExecutionNode parent) {
        ExecutionNode node = new ExecutionNode(recipeNode);
        node.parent = parent;
        for (RecipeNode childRecipe : recipeNode.getChildren()) {
            node.children.add(buildTree(childRecipe, node));
        }
        return node;
    }

    public RecipeNode getRecipeNode() {
        return this.recipeNode;
    }

    public Progress getProgress() {
        return this.progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    @Nullable
    public Resolution getResolution() {
        return this.resolution;
    }

    public void setResolution(@Nullable Resolution resolution) {
        this.resolution = resolution;
    }

    @Nullable
    public ExecutionNode getParent() {
        return this.parent;
    }

    public List<ExecutionNode> getChildren() {
        return this.children;
    }

    public int getCount() { return this.recipeNode.getCount(); }

    public Ingredient getIngredient() { return this.recipeNode.getIngredient(); }

    @Nullable
    public ICookCapability getCapability() { return this.recipeNode.getCapability(); }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void applyCount(IRecipeLookup recipes, int count) { this.recipeNode.applyCount(recipes, count); }

    public int calculateRequiredCount(WorldContext world) {
        return this.recipeNode.calculateCount(world);
    }

    @Nullable
    public ExecutionNode findNode(Progress target) {
        if (this.progress == target) {
            return this;
        }

        for (ExecutionNode child : children) {
            ExecutionNode found = child.findNode(target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
