package com.mastermarisa.maid_restaurant.request.world;

import com.mastermarisa.maid_restaurant.api.request.RequestHandler;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;

public class WorldCookRequestHandler extends RequestHandler<CookRequest> {
    @Override
    protected CookRequest fromCompound(HolderLookup.Provider provider, CompoundTag tag) {
        CookRequest request = new CookRequest();
        request.deserializeNBT(provider,tag);
        return request;
    }

    public static final AttachmentType<WorldCookRequestHandler> TYPE = AttachmentType.serializable(WorldCookRequestHandler::new).build();
}
