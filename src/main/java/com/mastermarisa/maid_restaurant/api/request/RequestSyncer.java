package com.mastermarisa.maid_restaurant.api.request;

import com.mastermarisa.maid_restaurant.api.functional.Instancer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class RequestSyncer <T extends IRequest, F extends RequestHandler<T>> implements AttachmentSyncHandler<F> {
    private final Instancer<F> instancer;

    public RequestSyncer(Instancer<F> instancer) {
        this.instancer = instancer;
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, F tRequestHandler, boolean b) {
        registryFriendlyByteBuf.writeInt(tRequestHandler.size());
        for (IRequest request : tRequestHandler.requests)
            registryFriendlyByteBuf.writeJsonWithCodec(CompoundTag.CODEC,request.serializeNBT(registryFriendlyByteBuf.registryAccess()));
        registryFriendlyByteBuf.writeBoolean(tRequestHandler.accept);
    }

    @Override
    public @Nullable F read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable F tRequestHandler) {
        if (tRequestHandler == null) tRequestHandler = instancer.instance();
        int count = registryFriendlyByteBuf.readInt();
        for (int i = 0;i < count;i++)
            tRequestHandler.add(tRequestHandler.fromCompound(registryFriendlyByteBuf.registryAccess(),registryFriendlyByteBuf.readJsonWithCodec(CompoundTag.CODEC)));
        tRequestHandler.accept = registryFriendlyByteBuf.readBoolean();
        return tRequestHandler;
    }
}
