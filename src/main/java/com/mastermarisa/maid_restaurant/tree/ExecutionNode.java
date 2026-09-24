package com.mastermarisa.maid_restaurant.tree;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

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

    public RecipeNode getRecipeNode() {
        return this.recipeNode;
    }

    public NodeState getState() {
        return this.state;
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
    public Recipe<?> getRecipe(RecipeManager recipeManager) { return this.recipeNode.getRecipe(recipeManager); }

    @Nullable
    public ICookCapability getCapability() { return this.recipeNode.getCapability(); }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public void applyCount(Level level, int count) { this.recipeNode.applyCount(level, count); }

    public int calculateRequiredCount(ServerLevel level, EntityMaid maid) {
        return this.recipeNode.calculateCount(level, maid);
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

    public void computeParentState() {
        if (this.parent != null) {
            this.parent.computeState();;
        }
    }

    /**
     * 验证并更新自身及子树状态
     * @param maid 女仆实体
     */
    public void verifyAndUpdateState(ServerLevel level, EntityMaid maid) {
        int required = parent != null ? parent.calculateRequiredCount(level, maid) : 0;
        verifyAndUpdateState(level, maid, required, null);
    }

    private void verifyAndUpdateState(ServerLevel level, EntityMaid maid, int parentCount,
                                      @Nullable List<ItemStack> existed) {
        int required = RecipeNode.calculateCountByParent(level, maid, recipeNode, parentCount);
        if (required > 0) {
            if (existed == null || existed.isEmpty()) {
                existed = ChefScheduler.getExistedInputs(level, maid, parent);
            }
            required -= InvUtil.count(existed, recipeNode.getIngredient());
        }

        if (isLeaf()) {
            state = required <= 0 ? NodeState.DONE : NodeState.NEED_MATERIALS;
        } else if (required <= 0) {
            // 只要自身满足条件,就不再关心子树状态,并将子树所有节点状态覆盖为 DONE
            state = NodeState.DONE;
            setSubtreeState(NodeState.DONE);
        } else {
            // 否则临时设为 WAITING,等待子树更新状态后重新推导
            state = NodeState.WAITING;
            for (ExecutionNode child : children) {
                child.verifyAndUpdateState(level, maid, required, existed);
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
}