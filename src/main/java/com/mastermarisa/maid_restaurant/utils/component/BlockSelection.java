package com.mastermarisa.maid_restaurant.utils.component;

import com.mastermarisa.maid_restaurant.utils.EncodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockSelection implements INBTSerializable<CompoundTag> {
    public List<BlockPos> menu = new ArrayList<>();
    public List<BlockPos> order = new ArrayList<>();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("menu", EncodeUtils.encode(menu));
        tag.putLongArray("order", EncodeUtils.encode(order));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("menu")) menu = new ArrayList<>(EncodeUtils.decode(tag.getLongArray("menu")));
        if (tag.contains("order")) order = new ArrayList<>(EncodeUtils.decode(tag.getLongArray("order")));
    }

    public static class Syncer implements AttachmentSyncHandler<BlockSelection> {
        @Override
        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, BlockSelection selection, boolean b) {
            registryFriendlyByteBuf.writeLongArray(EncodeUtils.encode(selection.menu));
            registryFriendlyByteBuf.writeLongArray(EncodeUtils.encode(selection.order));
        }

        @Override
        public @Nullable BlockSelection read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable BlockSelection selection) {
            if (selection == null) selection = new BlockSelection();
            selection.menu = new ArrayList<>(EncodeUtils.decode(registryFriendlyByteBuf.readLongArray()));
            selection.order = new ArrayList<>(EncodeUtils.decode(registryFriendlyByteBuf.readLongArray()));
            return selection;
        }
    }

    public static final AttachmentType<BlockSelection> TYPE = AttachmentType.
            serializable(BlockSelection::new).sync(new Syncer()).build();
}
