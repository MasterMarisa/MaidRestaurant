package com.mastermarisa.maid_restaurant.core.tree;

import com.google.gson.JsonElement;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipeNode implements INBTSerializable<CompoundTag> {
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_OUTPUT_COUNT = "output_count";
    private static final String TAG_STEP = "step";
    private static final String TAG_CHILDREN = "children";

    private Ingredient output;
    private int count;
    @Nullable
    private RecipeStep step;
    private List<RecipeNode> children;

    public RecipeNode() {
        this.output = Ingredient.EMPTY;
        this.children = new ArrayList<>();
    }

    public RecipeNode(Ingredient output, int count, @Nullable RecipeStep step) {
        this.output = output;
        this.count = count;
        this.step = step != null ? step.copy() : null;
        this.children = new ArrayList<>();
    }

    public Ingredient getOutput() {
        return output;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public RecipeStep getStep() {
        return step;
    }

    public List<RecipeNode> getChildren() {
        return children;
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

    public boolean isEmpty() { return output.isEmpty(); }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void setOutput(Ingredient output, int outputCount) {
        this.output = output;
        this.count = outputCount;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setStep(@Nullable RecipeStep step) {
        this.step = step != null ? step.copy() : null;
    }

    public void addChild(RecipeNode child) {
        children.add(child);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!output.isEmpty()) {
            tag.putString(TAG_OUTPUT, output.toJson().toString());
        }
        tag.putInt(TAG_OUTPUT_COUNT, count);

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
            JsonElement jsonElement = GsonHelper.parse(tag.getString(TAG_OUTPUT));
            output = Ingredient.fromJson(jsonElement);
        } else {
            output = Ingredient.EMPTY;
        }

        if (tag.contains(TAG_OUTPUT_COUNT)) {
            count = tag.getInt(TAG_OUTPUT_COUNT);
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
                children.add(child);
            }
        }
    }

    public static RecipeNode fromNBT(CompoundTag tag) {
        RecipeNode node = new RecipeNode();
        node.deserializeNBT(tag);
        return node;
    }
}
