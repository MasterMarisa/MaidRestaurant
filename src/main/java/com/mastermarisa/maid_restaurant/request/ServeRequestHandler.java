package com.mastermarisa.maid_restaurant.request;

import com.mastermarisa.maid_restaurant.api.request.RequestHandler;
import com.mastermarisa.maid_restaurant.api.request.RequestSyncer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ServeRequestHandler extends RequestHandler<ServeRequest> {
    @Override
    protected ServeRequest fromCompound(HolderLookup.Provider provider, CompoundTag tag) {
        ServeRequest request = new ServeRequest();
        request.deserializeNBT(provider,tag);
        return request;
    }

    public static final AttachmentType<ServeRequestHandler> TYPE = AttachmentType.
            serializable(ServeRequestHandler::new).sync(new RequestSyncer<>(ServeRequestHandler::new)).build();
}
