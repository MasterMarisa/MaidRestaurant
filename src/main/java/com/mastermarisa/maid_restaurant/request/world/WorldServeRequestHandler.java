package com.mastermarisa.maid_restaurant.request.world;

import com.mastermarisa.maid_restaurant.api.request.RequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;

public class WorldServeRequestHandler extends RequestHandler<ServeRequest> {
    @Override
    protected ServeRequest fromCompound(HolderLookup.Provider provider, CompoundTag tag) {
        ServeRequest request = new ServeRequest();
        request.deserializeNBT(provider,tag);
        return request;
    }

    public static final AttachmentType<WorldServeRequestHandler> TYPE = AttachmentType.serializable(WorldServeRequestHandler::new).build();
}
