package com.mastermarisa.maid_restaurant.core.tree;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.world.IRecipeLookup;
import com.mastermarisa.maid_restaurant.core.world.WorldContext;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.util.INBTSerializable;

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
    private IngredientStack cachedIngredient;
    @Nullable
    private Recipe<?> cachedRecipe;

    public RecipeNode() {
        this.ingredient = Ingredient.EMPTY;
        this.children = new ArrayList<>();
    }

    public RecipeNode(Ingredient ingredient, int count, @Nullable RecipeStep step) {
        this.ingredient = ingredient;
        this.count = count;
        this.step = step;
        this.children = new ArrayList<>();
    }

    public RecipeNode copy() {
        RecipeNode copy = new RecipeNode();
        copy.ingredient = this.ingredient;
        copy.count = this.count;
        copy.step = this.step;
        copy.parent = null;
        copy.cachedIngredient = this.cachedIngredient;

        for (RecipeNode child : this.children) {
            RecipeNode childCopy = child.copy();
            childCopy.parent = copy;
            copy.children.add(childCopy);
        }

        return copy;
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

    @Nullable
    public Recipe<?> getRecipe(IRecipeLookup recipes) {
        if (cachedRecipe == null) {
            if (step != null) {
                cachedRecipe = recipes.byKey(step.recipeId());
            }
        }
        return cachedRecipe;
    }

    @Nullable
    public ICookCapability getCapability() {
        if (step != null) {
            return CapabilityRegistry.get(step.capabilityID());
        }
        return null;
    }

    @Nullable
    public IngredientStack getCachedIngredient() {
        if (this.parent != null && this.cachedIngredient == null) {
            RecipeStep step = parent.getStep();
            if (step != null) {
                this.cachedIngredient = RecipeCacheBuilder.findStack(step.recipeId(), getIngredient());
            }
        }
        return this.cachedIngredient;
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
        this.cachedRecipe = null;
    }

    public void addChild(RecipeNode child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * 设置节点需求的输出物品数,并据此推导更新子树的配方倍率
     * @param recipes 配方查询端口
     * @param count 新的输出物品数
     */
    public void applyCount(IRecipeLookup recipes, int count) {
        this.count = count;
        recalculateSubtree(recipes, this, null);
    }

    private static void recalculateSubtree(IRecipeLookup recipes, RecipeNode node, @Nullable RecipeNode parent) {
        if (parent != null) {
            ICookCapability capability = parent.getCapability();
            Recipe<?> recipe = parent.getRecipe(recipes);
            if (recipe != null && capability != null) {
                IngredientStack stack = node.getCachedIngredient();
                if (stack != null) {
                    int count = capability.getIngredientCount(recipe, parent.getCount(), stack, recipes.registries());
                    node.setCount(count);
                }
            }
        }

        for (var child : node.getChildren()) {
            recalculateSubtree(recipes, child, node);
        }
    }

    /**
     * 根据上层节点的状态计算该节点缺少的材料数量,注意其中已计入 {@link WorldContext} 报告的已就绪物品数
     * @param world 只读世界上下文(物品就绪量 + 配方查询)
     * @return 需从外界(容器、合成)获取的材料数量
     */
    public int calculateCount(WorldContext world) {
        List<RecipeNode> nodes = new ArrayList<>();
        RecipeNode tmp = this;
        while (tmp != null) {
            nodes.add(tmp);
            tmp = tmp.parent;
        }

        int required = calculateCountByParent(world, nodes.get(nodes.size() - 1), 0);
        for (int i = nodes.size() - 2; i >= 0 && required > 0; i--) {
            required = calculateCountByParent(world, nodes.get(i), required);
        }

        return required;
    }

    public static int calculateCountByParent(WorldContext world, RecipeNode node, int parentCount) {
        if (node.getParent() == null) {
            int count = world.items().held(node.getIngredient());
            return Math.max(0, node.getCount() - count);
        }

        if (parentCount <= 0) {
            return 0;
        }

        RecipeNode parent = node.getParent();
        Recipe<?> recipe = parent.getRecipe(world.recipes());
        if (recipe == null) {
            return 0;
        }

        ICookCapability capability = parent.getCapability();
        IngredientStack stack = node.getCachedIngredient();
        if (capability == null || stack == null) {
            return 0;
        }

        int required = capability.getIngredientCount(recipe, parentCount, stack, world.recipes().registries());
        int count = world.items().held(node.getIngredient());
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
