package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.network.CookOrderPayload;
import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import com.mastermarisa.maid_restaurant.uitls.EncodeUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ServeRequestQueueSyncHandler implements AttachmentSyncHandler<ServeRequestQueue> {
    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, ServeRequestQueue serveRequestQueue, boolean b) {
        List<long[]> tables = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<String> uuids = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        List<Integer> requestedCounts = new ArrayList<>();
        for (Iterator<ServeRequest> it = serveRequestQueue.iterator(); it.hasNext(); ) {
            var request = it.next();
            tables.add(BlockPosUtils.pack(request.targetTables));
            ids.add(EncodeUtils.encode(request.toServe).toString());
            uuids.add(request.provider.toString());
            counts.add(request.toServe.getCount());
            requestedCounts.add(request.requestedCount);
        }
        registryFriendlyByteBuf.writeCollection(tables, CookOrderPayload.LONG_ARRAY_STREAM_CODEC);
        registryFriendlyByteBuf.writeCollection(ids, FriendlyByteBuf::writeUtf);
        registryFriendlyByteBuf.writeCollection(uuids, FriendlyByteBuf::writeUtf);
        registryFriendlyByteBuf.writeCollection(counts, FriendlyByteBuf::writeInt);
        registryFriendlyByteBuf.writeCollection(requestedCounts, FriendlyByteBuf::writeInt);
    }

    @Override
    public @Nullable ServeRequestQueue read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable ServeRequestQueue serveRequestQueue) {
        if (serveRequestQueue == null) serveRequestQueue = new ServeRequestQueue();
        List<long[]> tables = registryFriendlyByteBuf.readCollection(ArrayList::new, CookOrderPayload.LONG_ARRAY_STREAM_CODEC);
        List<String> ids = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<String> uuids = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
        List<Integer> counts = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
        List<Integer> requestedCounts = registryFriendlyByteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
        serveRequestQueue.clear();
        for (int i = 0;i < tables.size();i++) {
            serveRequestQueue.add(new ServeRequest(new ItemStack(EncodeUtils.decode(ids.get(i)),counts.get(i)),tables.get(i), UUID.fromString(uuids.get(i)),requestedCounts.get(i)));
        }
        return serveRequestQueue;
    }
}
