package com.mastermarisa.maid_restaurant.tree;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipeNode implements INBTSerializable<CompoundTag> {
    private static final String TAG_OUTPUT = "ingredient";
    private static final String TAG_COUNT = "count";
    private static final String TAG_STEP = "step";
    private static final String TAG_CHILDREN = "children";

    private Ingredient ingredient;
    private int count;
    @Nullable
    private RecipeStep step;
    @Nullable
    private RecipeNode parent;
    private List<RecipeNode> children;
    @Nullable
    private IngredientStack cachedStack;

    public RecipeNode() {
        this.ingredient = Ingredient.EMPTY;
        this.children = new ArrayList<>();
    }

    public RecipeNode(Ingredient ingredient, int count, @Nullable RecipeStep step) {
        this.ingredient = ingredient;
        this.count = count;
        this.step = step != null ? step.copy() : null;
        this.children = new ArrayList<>();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public RecipeStep getStep() {
        return step;
    }

    @Nullable
    public RecipeNode getParent() { return parent; }

    public List<RecipeNode> getChildren() {
        return children;
    }

    public void collectLeafIngredients(List<IngredientStack> result) {
        if (this.isLeaf()) {
            result.add(new IngredientStack(ingredient, count));
        }

        for (var child : children) {
            child.collectLeafIngredients(result);
        }
    }

    @Nullable
    public Recipe<?> getRecipe(RecipeManager recipeManager) {
        if (step != null) {
            return recipeManager.byKey(step.recipeId()).orElse(null);
        }
        return null;
    }

    @Nullable
    public ICookCapability getCapability() {
        if (step != null) {
            return CapabilityRegistry.get(step.capabilityID());
        }
        return null;
    }

    @Nullable
    public IngredientStack getCachedStack() {
        if (this.parent != null && this.cachedStack == null) {
            RecipeStep step = parent.getStep();
            if (step != null) {
                this.cachedStack = RecipeCacheBuilder.findStack(step.recipeId(), getIngredient());
            }
        }
        return this.cachedStack;
    }

    public IngredientStack getOutputAsStack() {
        return new IngredientStack(ingredient, count);
    }

    public boolean isEmpty() { return ingredient.isEmpty(); }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setStep(@Nullable RecipeStep step) {
        this.step = step != null ? step.copy() : null;
    }

    public void addChild(RecipeNode child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * 设置节点需求的输出物品数,并据此推导更新子树的配方倍率
     * @param level 所在Level
     * @param count 新的输出物品数
     */
    public void applyCount(Level level, int count) {
        this.count = count;
        recalculateSubtree(level, this, null);
    }

    private static void recalculateSubtree(Level level, RecipeNode node, @Nullable RecipeNode parent) {
        if (parent != null) {
            ICookCapability capability = parent.getCapability();
            Recipe<?> recipe = parent.getRecipe(level.getRecipeManager());
            if (recipe != null && capability != null) {
                IngredientStack stack = node.getCachedStack();
                if (stack != null) {
                    int count = capability.getIngredientCount(level, recipe, parent.getCount(), stack);
                    node.setCount(count);
                }
            }
        }

        for (var child : node.getChildren()) {
            recalculateSubtree(level, child, node);
        }
    }

    /**
     * 根据上层节点的状态计算该节点缺少的材料数量,注意其中已计入女仆背包中的对应物品数
     * @param level 所在Level
     * @param maid 女仆
     * @return 需从外界(容器、合成)获取的材料数量
     */
    public int calculateCount(ServerLevel level, EntityMaid maid) {
        List<RecipeNode> nodes = new ArrayList<>();
        RecipeNode tmp = this;
        while (tmp != null) {
            nodes.add(tmp);
            tmp = tmp.parent;
        }

        int required = calculateCountByParent(level, maid, nodes.get(nodes.size() - 1), 0);
        for (int i = nodes.size() - 2; i >= 0 && required > 0; i--) {
            required = calculateCountByParent(level, maid, nodes.get(i), required);
        }

        return required;
    }

    public static int calculateCountByParent(ServerLevel level, EntityMaid maid, RecipeNode node, int parentCount) {
        if (node.getParent() == null) {
            int count = InvUtil.count(maid.getAvailableInv(false), node.getIngredient());
            return Math.max(0, node.getCount() - count);
        }

        if (parentCount <= 0) {
            return 0;
        }

        RecipeNode parent = node.getParent();
        Recipe<?> recipe = parent.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return 0;
        }

        ICookCapability capability = parent.getCapability();
        IngredientStack stack = node.getCachedStack();
        if (capability == null || stack == null) {
            return 0;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        int required = capability.getIngredientCount(level, recipe, parentCount, stack);
        int count = InvUtil.count(maidInv, node.getIngredient());
        return Math.max(0, required - count);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!ingredient.isEmpty()) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            ingredient.toNetwork(buffer);
            tag.putByteArray(TAG_OUTPUT, buffer.array());
        }
        tag.putInt(TAG_COUNT, count);

        if (step != null) {
            tag.put(TAG_STEP, step.serializeNBT());
        }

        if (!children.isEmpty()) {
            ListTag childrenTag = new ListTag();
            for (RecipeNode child : children) {
                childrenTag.add(child.serializeNBT());
            }
            tag.put(TAG_CHILDREN, childrenTag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_OUTPUT)) {
            byte[] bytes = tag.getByteArray(TAG_OUTPUT);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            ingredient = Ingredient.fromNetwork(buffer);
        } else {
            ingredient = Ingredient.EMPTY;
        }

        if (tag.contains(TAG_COUNT)) {
            count = tag.getInt(TAG_COUNT);
        }

        if (tag.contains(TAG_STEP)) {
            step = RecipeStep.fromNBT(tag.getCompound(TAG_STEP));
        } else {
            step = null;
        }

        children = new ArrayList<>();
        if (tag.contains(TAG_CHILDREN)) {
            ListTag childrenTag = tag.getList(TAG_CHILDREN, Tag.TAG_COMPOUND);
            for (int i = 0; i < childrenTag.size(); i++) {
                RecipeNode child = new RecipeNode();
                child.deserializeNBT(childrenTag.getCompound(i));
                addChild(child);
            }
        }
    }

    public static RecipeNode fromNBT(CompoundTag tag) {
        RecipeNode node = new RecipeNode();
        node.deserializeNBT(tag);
        return node;
    }
}
