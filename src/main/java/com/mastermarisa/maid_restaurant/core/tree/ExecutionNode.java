package com.mastermarisa.maid_restaurant.core.tree;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Ingredient;

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

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    /**
     * 自推导自身及子树状态
     */
    public void computeState() {
        //如果 node 是叶节点，则保持当前状态（由外部设置）
        if (isLeaf()) {
            return;
        }

        // 先递归 computeState 所有子节点
        for (ExecutionNode child : children) {
            child.computeState();
        }

        boolean allChildrenDone = true;

        for (ExecutionNode child : children) {
            if (child.state != NodeState.DONE) {
                allChildrenDone = false;
                break;
            }
        }

        // 如果所有子节点 == DONE 且 本节点非 EXECUTING & DONE → 本节点 = READY
        if (allChildrenDone) {
            if (state != NodeState.EXECUTING && state != NodeState.DONE) {
                state = NodeState.READY;
            }
        } else {
            // 如果任何子节点 != DONE → 本节点 = WAITING
            state = NodeState.WAITING;
        }
    }

    /**
     * 验证前置条件并在不符合时回退自身及子树状态
     * @param level 所处Level
     * @param maid 女仆实体
     */
    public void verifyAndRollback(ServerLevel level, EntityMaid maid) {
        if (isLeaf()) {
            Ingredient ingredient = recipeNode.getOutput();
            boolean containing = ItemUtils.count(maid.getAvailableInv(false), ingredient) >= recipeNode.getOutputCount();
            if (!containing) {
                state = NodeState.NEED_MATERIALS;
            }
            return;
        }

        List<ExecutionNode> unreadyChildren = Lists.newArrayList();
        if (!isReadyForExecution(level, maid, unreadyChildren)) {
            state = NodeState.WAITING;
            for (var child : unreadyChildren) {
                child.verifyAndRollback(level, maid);
            }
        }
    }

    /**
     * @param level 所处Level
     * @param maid 女仆实体
     * @param unreadyChildren 函数会将未满足需求的子节点存入该List
     * @return 深一层节点需求是否全部完成
     */
    public boolean isReadyForExecution(ServerLevel level, EntityMaid maid, List<ExecutionNode> unreadyChildren) {
        boolean allChildrenDone = true;
        for (var child : children) {
            Ingredient ingredient = child.recipeNode.getOutput();
            boolean containing = ItemUtils.count(maid.getAvailableInv(false), ingredient) >= child.recipeNode.getOutputCount();
            if (!containing) {
                allChildrenDone = false;
                unreadyChildren.add(child);
            }
        }
        return allChildrenDone;
    }

    /**
     * @return 树中任意一个状态为 NEED_MATERIALS 的叶节点
     */
    @Nullable
    public ExecutionNode findNeedMaterialNode() {
        MaidRestaurant.LOGGER.debug("State:" + state.name());
        if (isLeaf()) {
            return state == NodeState.NEED_MATERIALS ? this : null;
        }

        for (var child : children) {
            ExecutionNode found = child.findNeedMaterialNode();
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * @return 树中任意一个状态为 READY 的节点
     */
    @Nullable
    public ExecutionNode findReadyNode() {
        if (state == NodeState.READY) {
            return this;
        }

        for (var child : children) {
            ExecutionNode found = child.findReadyNode();
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * @return 树中任意一个状态为 EXECUTING 的节点
     */
    @Nullable
    public ExecutionNode findExecutingNode() {
        if (state == NodeState.EXECUTING) {
            return this;
        }

        for (var child : children) {
            ExecutionNode found = child.findExecutingNode();
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}