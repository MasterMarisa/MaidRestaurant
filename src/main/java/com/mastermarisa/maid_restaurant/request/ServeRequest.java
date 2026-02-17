package com.mastermarisa.maid_restaurant.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.request.IRequest;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.EncodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServeRequest implements IRequest {
    public static final int TYPE = 1;

    public ItemStack toServe;
    public LinkedList<BlockPos> targets;
    public UUID provider;
    public int requested;

    public ServeRequest() {}

    public ServeRequest(ItemStack toServe, long[] targets, UUID provider, int requested) {
        this.toServe = toServe;
        this.targets = new LinkedList<>(EncodeUtils.decode(targets));
        this.provider = provider;
        this.requested = requested;
    }

    public ServeRequest(ItemStack toServe, long[] targets, UUID provider) {
        this(toServe,targets,provider,toServe.getCount());
    }

    @Override
    public boolean checkEnableConditions(ServerLevel level, EntityMaid maid) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("targets",targets.stream().map(BlockPos::asLong).toList());
        tag.putString("item",EncodeUtils.encode(toServe).toString());
        tag.putInt("count",toServe.getCount());
        tag.putString("provider", this.provider.toString());
        tag.putInt("requested", requested);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("targets")) targets = new LinkedList<>(EncodeUtils.decode(tag.getLongArray("targets")));
        if (tag.contains("item")) toServe = new ItemStack(EncodeUtils.decode(tag.getString("item")),tag.getInt("count"));
        if (tag.contains("provider")) this.provider = UUID.fromString(tag.getString("provider"));
        requested = tag.getInt("requested");
    }

    public static ServeRequest from(Level level, UUID uuid, CookRequest request) {
        ServeRequest serveRequest = new ServeRequest();
        ICookTask iCookTask = CookTasks.getTask(request.type);
        ItemStack result = iCookTask.getResult(level.getRecipeManager().byKey(request.id).get(),level);
        serveRequest.toServe = result.copyWithCount(result.getCount() * request.requested);
        serveRequest.targets = new LinkedList<>(EncodeUtils.decode(request.targets));
        serveRequest.provider = uuid;
        serveRequest.requested = request.requested;
        return serveRequest;
    }
}
