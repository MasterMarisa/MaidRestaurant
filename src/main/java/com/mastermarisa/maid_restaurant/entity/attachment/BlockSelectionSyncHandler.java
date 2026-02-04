package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockSelectionSyncHandler implements AttachmentSyncHandler<BlockSelection> {
    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, BlockSelection selection, boolean b) {
        registryFriendlyByteBuf.writeLongArray(BlockPosUtils.pack(selection.selected));
    }

    @Override
    public @Nullable BlockSelection read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable BlockSelection selection) {
        if (selection == null) selection = new BlockSelection();
        selection.selected = BlockPosUtils.unpack(registryFriendlyByteBuf.readLongArray());
        return selection;
    }
}
