package com.mastermarisa.maid_restaurant.data.menu;

import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuEntry implements INBTSerializable<CompoundTag> {
    private static final String TAG_ROOT = "root";
    private static final String TAG_NAME = "name";

    private RecipeNode root;
    private String name;
    @Nullable
    private RecipeInfo info;

    public MenuEntry() {}

    public MenuEntry(RecipeNode root, String name) {
        this.root = root;
        this.name = name;
    }

    public void setRoot(RecipeNode root) {
        this.root = root;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecipeNode getRoot() {
        return this.root;
    }

    public String getName() {
        return this.name;
    }

    public @NotNull RecipeInfo getInfo() {
        if (this.info == null) {
            this.info = RecipeInfo.fromNode(this.root);
        }
        return this.info;
    }

    public MenuEntry copy() {
        MenuEntry entry = new MenuEntry(this.root, this.name);
        entry.info = this.info;
        return entry;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ROOT, this.root.serializeNBT());
        tag.putString(TAG_NAME, this.name);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_ROOT)) {
            this.root = RecipeNode.fromNBT(tag.getCompound(TAG_ROOT));
        }
        if (tag.contains("name")) {
            this.name = tag.getString(TAG_NAME);
        }
    }
}
