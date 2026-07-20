package com.mastermarisa.maid_restaurant.core.tree;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequest;
import com.mastermarisa.maid_restaurant.core.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExecutionNode {
    private final RecipeNode recipeNode;
    private NodeState state;
    @Nullable
    private ExecutionNode parent;
    private final List<ExecutionNode> children;

    public ExecutionNode(RecipeNode recipeNode) {
        this.recipeNode = recipeNode;
        this.state = NodeState.NEED_MATERIALS;
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

    private static void recalculateSubtree(Level level, RecipeNode node, @Nullable RecipeNode parent) {
        if (parent != null) {
            ICookCapability capability = parent.getCapability();
            Recipe<?> recipe = parent.getRecipe(level.getRecipeManager());
            if (recipe != null && capability != null) {
                IngredientStack stack = RecipeCacheBuilder.findStack(recipe.getId(), node.getOutput());
                if (stack != null) {
                    int count = capability.getIngredientCount(level, recipe, parent.getCount(), stack);
                    node.setOutput(stack.getIngredient(), count);
                }
            }
        }

        for (var child : node.getChildren()) {
            recalculateSubtree(level, child, node);
        }
    }

    public RecipeNode getRecipeNode() {
        return recipeNode;
    }

    public NodeState getState() {
        return state;
    }

    @Nullable
    public ExecutionNode getParent() {
        return parent;
    }

    public List<ExecutionNode> getChildren() {
        return children;
    }

    @Nullable
    public Recipe<?> getRecipe(RecipeManager recipeManager) { return recipeNode.getRecipe(recipeManager); }

    @Nullable
    public ICookCapability getCapability() { return recipeNode.getCapability(); }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    /**
     * 根据子节点自推导自身状态
     */
    public void computeState() {
        //如果为叶节点,则保持当前状态
        if (isLeaf()) {
            return;
        }

        boolean allChildrenDone = true;
        for (ExecutionNode child : children) {
            if (child.state != NodeState.DONE) {
                allChildrenDone = false;
                break;
            }
        }

        // 如果所有子节点都已完成且本节点非 EXECUTING & DONE → 本节点转为 READY
        if (allChildrenDone) {
            if (state != NodeState.EXECUTING && state != NodeState.DONE) {
                state = NodeState.READY;
            }
        } else {
            // 如果任何子节点未完成,则继续等待
            state = NodeState.WAITING;
        }
    }

    /**
     * 验证并更新自身及子树状态
     * @param maid 女仆实体
     */
    public void verifyAndUpdateState(ServerLevel level, EntityMaid maid) {
        IItemHandler handler = maid.getAvailableInv(false);
        int count = recipeNode.getCount();
        if (parent == null) {
            RequestBus<CookingRequest> bus = RequestBus.getInstance(level, CookingRequest.class);
            String restaurantId = ChefScheduler.getRestaurantId(maid);
            if (restaurantId != null) {
                CookingRequest request = bus.getClaimed(restaurantId, maid);
                count = request != null ? request.count : count;
            }
        }
        boolean containing = ItemUtils.contains(handler, recipeNode.getOutput(), count);

        if (isLeaf()) {
            state = containing ? NodeState.DONE : NodeState.NEED_MATERIALS;
        } else if (containing) {
            // 只要自身满足条件,就不再关心子树状态,并将子树所有节点状态覆盖为 DONE
            state = NodeState.DONE;
            setSubtreeState(NodeState.DONE);
        } else {
            // 否则临时设为 WAITING,等待子树更新状态后重新推导
            state = NodeState.WAITING;
            for (ExecutionNode child : children) {
                child.verifyAndUpdateState(level, maid);
            }
            computeState();
        }
    }

    /**
     * 将子树中的所有节点设置为目标状态
     * @param state 目标状态
     */
    public void setSubtreeState(NodeState state) {
        for (ExecutionNode child : children) {
            child.setState(state);
            child.setSubtreeState(state);
        }
    }

    /**
     * 自顶向下搜索对应状态的节点
     * @param state 目标状态
     * @return 找到的节点
     */
    @Nullable
    public ExecutionNode findNode(NodeState state) {
        if (this.state == state) {
            return this;
        }

        for (ExecutionNode child : children) {
            ExecutionNode found = child.findNode(state);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void applyOutputCount(Level level, int count) {
        recipeNode.setCount(count);
        recalculateSubtree(level, this.getRecipeNode(), null);
    }
}