package com.mastermarisa.maid_restaurant.core.recipe;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.LinkedList;

public class ContextList implements INBTSerializable<CompoundTag> {
    private static final String TAG_CONTEXTS = "contexts";
    private static final String TAG_CURRENT_INDEX = "current_index";

    private LinkedList<RecipeExecutionContext> list;
    private int currentIndex;

    public ContextList() {
        this.list = new LinkedList<>();
        this.currentIndex = -1;
    }

    public LinkedList<RecipeExecutionContext> getList() {
        return list;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (RecipeExecutionContext context : list) {
            listTag.add(context.serializeNBT());
        }
        tag.put(TAG_CONTEXTS, listTag);
        tag.putInt(TAG_CURRENT_INDEX, currentIndex);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        list = new LinkedList<>();
        if (tag.contains(TAG_CONTEXTS)) {
            ListTag listTag = tag.getList(TAG_CONTEXTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                list.add(RecipeExecutionContext.fromNBT(listTag.getCompound(i)));
            }
        }
        currentIndex = tag.getInt(TAG_CURRENT_INDEX);
    }

    public static class DATA_KEY implements TaskDataKey<ContextList> {
        public static final ResourceLocation KEY = MaidRestaurant.resourceLocation("chef_scheduler_contexts");

        @Override
        public ResourceLocation getKey() {
            return KEY;
        }

        @Override
        public CompoundTag writeSaveData(ContextList contextList) {
            return contextList.serializeNBT();
        }

        @Override
        public ContextList readSaveData(CompoundTag compoundTag) {
            ContextList contextList = new ContextList();
            contextList.deserializeNBT(compoundTag);
            return contextList;
        }
    }
}
