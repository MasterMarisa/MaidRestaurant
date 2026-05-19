package com.mastermarisa.maid_restaurant.maid.behavior.chef;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.recipe.ContextList;
import com.mastermarisa.maid_restaurant.core.recipe.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeExecutionContext;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeNode;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class ChefScheduler {
    public static ContextList getContextList(EntityMaid maid) {
        ContextList contexts = maid.getData(ModTaskDataKeys.CHEF_CONTEXTS);
        if (contexts == null) {
            contexts = new ContextList();
            maid.setData(ModTaskDataKeys.CHEF_CONTEXTS, contexts);
        }
        return contexts;
    }

    /**
     * 阻塞当前上下文,并切换到下一个非阻塞上下文
     * @param maid 女仆实体
     */
    public static void switchToNextContext(EntityMaid maid) {
        ContextList contextList = getContextList(maid);
        LinkedList<RecipeExecutionContext> contexts = contextList.getList();
        if (contexts.isEmpty()) {
            return;
        }

        int current = contextList.getCurrentIndex();
        if (current >= 0 && current < contexts.size()) {
            contexts.get(current).setBlocked(true);
        }

        int next = (current + 1) % contexts.size();
        int attempts = 0;
        while (attempts < contexts.size()) {
            if (!contexts.get(next).isBlocked()) {
                contextList.setCurrentIndex(next);
                return;
            }
            next = (next + 1) % contexts.size();
            attempts++;
        }

        contextList.setCurrentIndex(-1);
    }

    /**
     * 验证所有阻塞上下文,如果树中存在状态为 READY 或 NEED_MATERIALS 的节点便解除阻塞
     * @param level 所处的Level
     * @param maid 女仆实体
     */
    public static void checkUnblock(ServerLevel level, EntityMaid maid) {
        ContextList contextList = getContextList(maid);

        for (var context : contextList.getList()) {
            if (!context.isBlocked()) {
                continue;
            }

            ExecutionNode root = context.getRoot();
            if (root == null) {
                continue;
            }

            root.verifyAndRollback(level, maid);
            root.computeState();
            ExecutionNode ready = root.findReadyNode();
            ExecutionNode needMaterial = root.findNeedMaterialNode();
            if (ready != null || needMaterial != null) {
                context.setBlocked(false);
            }
        }
    }

    /**
     * 检查当前上下文任务是否完成,并进行后续处理
     * @param level 所处的Level
     * @param maid 女仆实体
     */
    public static void checkAndSubmit(ServerLevel level, EntityMaid maid) {
        MaidRestaurant.LOGGER.debug("[MaidRestaurant-DEBUG] Context Submitted.");
        ContextList contextList = getContextList(maid);
        LinkedList<RecipeExecutionContext> contexts = contextList.getList();
        int current = contextList.getCurrentIndex();
        if (current >= 0 && current < contexts.size()) {
            RecipeExecutionContext context = contexts.get(current);
            RecipeNode recipeNode = context.getRoot().getRecipeNode();
            int count = ItemUtils.count(maid.getAvailableInv(false), recipeNode.getOutput());
            if (count >= recipeNode.getOutputCount()) {
                contexts.remove(current);
                contextList.setCurrentIndex(-1);
                checkUnblock(level, maid);
                switchToNextContext(maid);
                MaidRestaurant.LOGGER.debug("[MaidRestaurant-DEBUG] Context Resolved.");
            }
        }
    }

    /**
     * 尝试在当前上下文中搜索状态为 NEED_MATERIAL 的叶节点
     * @param maid 女仆实体
     * @return 状态为 NEED_MATERIAL 的叶节点
     */
    @Nullable
    public static ExecutionNode findNeedMaterialNode(EntityMaid maid) {
        ContextList contextList = getContextList(maid);
        LinkedList<RecipeExecutionContext> contexts = contextList.getList();
        int current = contextList.getCurrentIndex();
        if (current >= 0 && current < contexts.size()) {
            return contexts.get(current).getRoot().findNeedMaterialNode();
        }
        return null;
    }

    /**
     * 尝试在当前上下文中搜索状态为 READY 的节点
     * @param maid 女仆实体
     * @return 状态为 READY 的节点
     */
    @Nullable
    public static ExecutionNode findReadyNode(EntityMaid maid) {
        ContextList contextList = getContextList(maid);
        LinkedList<RecipeExecutionContext> contexts = contextList.getList();
        int current = contextList.getCurrentIndex();
        if (current >= 0 && current < contexts.size()) {
            return contexts.get(current).getRoot().findReadyNode();
        }
        return null;
    }

    /**
     * 尝试在当前上下文中搜索状态为 EXECUTING 的节点
     * @param maid 女仆实体
     * @return 状态为 EXECUTING 的节点
     */
    @Nullable
    public static ExecutionNode findExecutingNode(EntityMaid maid) {
        ContextList contextList = getContextList(maid);
        LinkedList<RecipeExecutionContext> contexts = contextList.getList();
        int current = contextList.getCurrentIndex();
        if (current >= 0 && current < contexts.size()) {
            return contexts.get(current).getRoot().findExecutingNode();
        }
        return null;
    }
}
