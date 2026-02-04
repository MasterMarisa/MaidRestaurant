package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CookRequestQueueSyncHandler implements AttachmentSyncHandler<CookRequestQueue> {
    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, CookRequestQueue queue, boolean b) {
        List<String> ID = new ArrayList<>();
        List<String> type = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        List<Integer> requestCount = new ArrayList<>();
        for (CookRequest request : queue.toList()) {
            ID.add(request.id.toString());
            type.add(CookTaskManager.getUID(request.type));
            count.add(request.count);
            requestCount.add(request.requestedCount);
        }
        registryFriendlyByteBuf.writeCollection(ID, FriendlyByteBuf::writeUtf);
        registryFriendlyByteBuf.writeCollection(type, FriendlyByteBuf::writeUtf);
        registryFriendlyByteBuf.writeCollection(count, FriendlyByteBuf::writeInt);
        registryFriendlyByteBuf.writeCollection(requestCount, FriendlyByteBuf::writeInt);
    }

    @Override
    public @Nullable CookRequestQueue read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable CookRequestQueue queue) {
        if (queue == null) queue = new CookRequestQueue();
        List<String> ID = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> type = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<Integer> count = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
        List<Integer> requestCount = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
        queue.clear();
        for (int i = 0;i < ID.size();i++) {
            queue.add(new CookRequest(ID.get(i),type.get(i),count.get(i),requestCount.get(i)));
        }

        return queue;
    }
}
