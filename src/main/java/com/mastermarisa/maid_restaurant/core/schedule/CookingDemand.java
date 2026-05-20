package com.mastermarisa.maid_restaurant.core.schedule;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CookingDemand implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";
    private static final String TAG_TARGET_POSITIONS = "target_positions";
    private static final String TAG_TEMP_DATA = "temp_data";
    private static final String TAG_BLOCKED = "blocked";
    private static final String TAG_ICON = "icon";
    private static final String TAG_DISPLAY_NAME = "display_name";

    private ExecutionNode root;
    private List<Long> targetPositions;
    private CompoundTag tempData;
    private ItemStack icon;
    private String displayName;

    public CookingDemand() {
        this.root = null;
        this.targetPositions = new ArrayList<>();
        this.tempData = new CompoundTag();
        this.icon = ItemStack.EMPTY;
        this.displayName = "";
    }

    public CookingDemand(RecipeNode recipeRoot) {
        this.root = ExecutionNode.fromRecipeTree(recipeRoot);
        this.targetPositions = new ArrayList<>();
        this.tempData = new CompoundTag();
        this.icon = ItemStack.EMPTY;
        this.displayName = "";
    }

    public ExecutionNode getRoot() {
        return root;
    }

    public void setRoot(ExecutionNode root) { this.root = root; }

    public List<Long> getTargetPositions() {
        return targetPositions;
    }

    public void setTargetPositions(List<Long> targetPositions) {
        this.targetPositions = new ArrayList<>(targetPositions);
    }

    public CompoundTag getTempData() {
        return tempData;
    }

    public void setTempData(CompoundTag tempData) {
        this.tempData = tempData;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, root.getRecipeNode().serializeNBT());
        tag.putLongArray(TAG_TARGET_POSITIONS, targetPositions);
        tag.put(TAG_TEMP_DATA, tempData);
        tag.put(TAG_ICON, icon.save(new CompoundTag()));
        tag.putString(TAG_DISPLAY_NAME, displayName);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            RecipeNode recipeRoot = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
            root = ExecutionNode.fromRecipeTree(recipeRoot);
            root.computeState();
        }
        if (tag.contains(TAG_TARGET_POSITIONS)) {
            targetPositions = Arrays.stream(tag.getLongArray(TAG_TARGET_POSITIONS)).boxed().toList();
        }
        if (tag.contains(TAG_TEMP_DATA)) {
            tempData = tag.getCompound(TAG_TEMP_DATA);
        }
        if (tag.contains(TAG_ICON)) {
            icon = ItemStack.of(tag.getCompound(TAG_ICON));
        }
        if (tag.contains(TAG_DISPLAY_NAME)) {
            displayName = tag.getString(TAG_DISPLAY_NAME);
        }
    }

    public static CookingDemand fromNBT(CompoundTag tag) {
        CookingDemand context = new CookingDemand();
        context.deserializeNBT(tag);
        return context;
    }
}
