package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class BlockSelection implements INBTSerializable<CompoundTag> {
    public List<BlockPos> selected = new ArrayList<>();

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public boolean isPresent() {
        return !selected.isEmpty();
    }

    public int size() {
        return selected.size();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("selected", BlockPosUtils.pack(selected));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("selected")) {
            selected = BlockPosUtils.unpack(tag.getLongArray("selected"));
        }
    }

    public static final AttachmentType<BlockSelection> TYPE = AttachmentType.serializable(BlockSelection::new).sync(new BlockSelectionSyncHandler()).build();
}
