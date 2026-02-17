package com.mastermarisa.maid_restaurant.request;

import com.mastermarisa.maid_restaurant.api.request.RequestHandler;
import com.mastermarisa.maid_restaurant.api.request.RequestSyncer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;

public class CookRequestHandler extends RequestHandler<CookRequest> {
    @Override
    public @Nullable CookRequest removeFirst() {
        if (requests.isEmpty()) return null;
        CookRequest request = requests.removeFirst();
        if (request.attributes.cycle()) {
            CookRequest toInsert = request.copy();
            requests.add(toInsert);
            toInsert.remain = toInsert.requested;
        }
        return request;
    }

    @Override
    protected CookRequest fromCompound(HolderLookup.Provider provider, CompoundTag tag) {
        CookRequest request = new CookRequest();
        request.deserializeNBT(provider,tag);
        return request;
    }

    public static final AttachmentType<CookRequestHandler> TYPE = AttachmentType
            .serializable(CookRequestHandler::new).sync(new RequestSyncer<>(CookRequestHandler::new)).build();
}
