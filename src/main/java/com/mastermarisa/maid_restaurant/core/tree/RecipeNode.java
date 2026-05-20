package com.mastermarisa.maid_restaurant.core.tree;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
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
    private int outputCount;
    @Nullable
    private RecipeStep combineStep;
    private List<RecipeNode> children;

    public RecipeNode() {
        this.output = Ingredient.EMPTY;
        this.outputCount = 1;
        this.combineStep = null;
        this.children = new ArrayList<>();
    }

    public RecipeNode(Ingredient output, int outputCount, @Nullable RecipeStep combineStep) {
        this.output = output;
        this.outputCount = outputCount;
        this.combineStep = combineStep != null ? combineStep.copy() : null;
        this.children = new ArrayList<>();
    }

    public Ingredient getOutput() {
        return output;
    }

    public int getOutputCount() {
        return outputCount;
    }

    @Nullable
    public RecipeStep getCombineStep() {
        return combineStep;
    }

    public List<RecipeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean hasCombineStep() {
        return combineStep != null;
    }

    public void setOutput(Ingredient output, int outputCount) {
        this.output = output;
        this.outputCount = outputCount;
    }

    public void setCombineStep(@Nullable RecipeStep step) {
        this.combineStep = step != null ? step.copy() : null;
    }

    public void addChild(RecipeNode child) {
        children.add(child);
    }

    public void setChildren(List<RecipeNode> children) {
        this.children = new ArrayList<>(children);
    }

    public RecipeNode deepCopy() {
        RecipeNode copy = new RecipeNode(output, outputCount, combineStep);
        for (RecipeNode child : children) {
            copy.addChild(child.deepCopy());
        }
        return copy;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!output.isEmpty()) {
            tag.putString(TAG_OUTPUT, output.toJson().toString());
        }
        tag.putInt(TAG_OUTPUT_COUNT, outputCount);

        if (combineStep != null) {
            tag.put(TAG_STEP, combineStep.serializeNBT());
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
            outputCount = tag.getInt(TAG_OUTPUT_COUNT);
        }

        if (tag.contains(TAG_STEP)) {
            combineStep = RecipeStep.fromNBT(tag.getCompound(TAG_STEP));
        } else {
            combineStep = null;
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
