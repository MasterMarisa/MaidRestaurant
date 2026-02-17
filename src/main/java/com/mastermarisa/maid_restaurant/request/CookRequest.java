package com.mastermarisa.maid_restaurant.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.api.request.IRequest;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CookRequest implements IRequest {
    public static final int TYPE = 0;

    public ResourceLocation id;
    public RecipeType<?> type;
    public int remain;
    public int requested;
    public long[] targets;
    public Attributes attributes = new Attributes();
    public CompoundTag extraData = new CompoundTag();

    public CookRequest() {}

    public CookRequest(ResourceLocation id, RecipeType<?> type, int remain, int requested, long[] targets, byte[] attributes) {
        this.id = id;
        this.type = type;
        this.remain = remain;
        this.requested = requested;
        this.targets = targets;
        this.attributes = new Attributes(attributes);
    }

    public CookRequest(String id, String type, int remain, int requested, long[] targets, byte[] attributes) {
        this(ResourceLocation.parse(id),CookTasks.getType(type),remain,requested,targets,attributes);
    }

    public CookRequest(String id, String type, int remain, int requested, List<Long> targets, List<Byte> attributes) {
        this(ResourceLocation.parse(id),CookTasks.getType(type),remain, requested,targets.stream().mapToLong(Long::longValue).toArray(), EncodeUtils.toArray(attributes));
    }

    public CookRequest(ResourceLocation id, RecipeType<?> type, int remain, int requested, long[] targets) {
        this(id,type,remain,requested,targets,new byte[]{});
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id",id.toString());
        tag.putString("type", CookTasks.getUID(type));
        tag.putInt("remain",remain);
        tag.putInt("requested",requested);
        tag.putLongArray("targets",targets);
        tag.putByteArray("attributes",attributes.getAttributes());
        tag.put("extra",extraData);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("id")) {
            id = ResourceLocation.parse(tag.getString("id"));
            type = CookTasks.getType(tag.getString("type"));
            remain = tag.getInt("remain");
            requested = tag.getInt("requested");
            targets = tag.getLongArray("targets");
            attributes = new Attributes(tag.getByteArray("attributes"));
            extraData = tag.getCompound("extra");
        }
    }

    @Override
    public boolean checkEnableConditions(ServerLevel level, EntityMaid maid) {
        switch (attributes.getStockingMode()) {
            case INSERTABLE -> {
                return Arrays.stream(targets).anyMatch(l -> checkTargetAvailability(level,maid, EncodeUtils.decode(l)));
            }
            case SPACE_ENOUGH -> {
                ICookTask iCookTask = CookTasks.getTask(type);
                ItemStack result = iCookTask.getResult(level.getRecipeManager().byKey(id).get(),level);
                boolean block = result.getItem() instanceof BlockItem;
                int slot = 0;
                for (int i = 0;i < targets.length && slot < requested * result.getCount();i++) {
                    BlockPos pos = EncodeUtils.decode(targets[i]);
                    int count = BlockUsageManager.getUserCount(pos);
                    if (count > 0 && !(BlockUsageManager.isUsing(pos,maid.getUUID()) && count == 1)) continue;
                    BlockState state = level.getBlockState(pos);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof TableBlockEntity table && level.getBlockState(pos.immutable().above()).canBeReplaced()) {
                        if (block) slot++;
                        else slot += getEmptySlots(table.getItems());
                    } else if (state.is(TagBlock.SERVE_MEAL_BLOCK) && level.getBlockState(pos.immutable().above()).canBeReplaced()) {
                        if (block) slot++;
                    } else {
                        IMaidStorage storage = MaidStorages.tryGetType(level,pos);
                        if (storage != null) {
                            slot += requested * result.getCount() - storage.insert(level,pos,result.copy(),true).getCount();
                        }
                    }
                }

                return slot >= requested * result.getCount();
            }
        }

        return true;
    }

    private static int getEmptySlots(ItemStackHandler itemStackHandler) {
        int count = 0;
        for (int i = 0;i < itemStackHandler.getSlots();i++)
            if (itemStackHandler.getStackInSlot(i).isEmpty())
                count++;

        return count;
    }

    private boolean checkTargetAvailability(ServerLevel level, EntityMaid maid, BlockPos pos) {
        int count = BlockUsageManager.getUserCount(pos);
        if (count > 0 && !(BlockUsageManager.isUsing(pos,maid.getUUID()) && count == 1)) return false;
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TableBlockEntity table)
            return level.getBlockState(pos.immutable().above()).canBeReplaced() && table.getItems().getStackInSlot(3).isEmpty();

        if (state.is(TagBlock.SERVE_MEAL_BLOCK) && level.getBlockState(pos.immutable().above()).canBeReplaced())
            return true;

        IMaidStorage storage = MaidStorages.tryGetType(level,pos);
        if (storage != null) {
            ICookTask iCookTask = CookTasks.getTask(type);
            ItemStack result = iCookTask.getResult(level.getRecipeManager().byKey(id).get(),level);
            return result.getCount() != storage.insert(level,pos,result.copy(),true).getCount();
        }

        return false;
    }

    public CookRequest copy() {
        CookRequest request = new CookRequest();
        request.id = id;
        request.remain = remain;
        request.requested = requested;
        request.type = type;
        request.targets = Arrays.copyOf(targets,targets.length);
        request.attributes = new Attributes(attributes.getAttributes());
        request.extraData = new CompoundTag();
        return request;
    }

    public static class Attributes {
        public static final int attributeCount = 2;

        private byte[] attributes;

        public Attributes() { attributes = new byte[attributeCount]; }

        public Attributes(byte[] attributes) {
            this.attributes = Arrays.copyOf(attributes,attributeCount);
        }

        public byte[] getAttributes() {
            return attributes;
        }

        public void setAttributes(byte[] attributes) { this.attributes = Arrays.copyOf(attributes,attributeCount); }

        public boolean cycle() { return attributes[0] == 1; }

        public void setCycle(boolean value) { attributes[0] = (byte) (value ? 1 : 0); }

        public StockingMode getStockingMode() {
            switch (attributes[1]) {
                case 1 -> {
                    return StockingMode.INSERTABLE;
                }
                case 2 -> {
                    return StockingMode.SPACE_ENOUGH;
                }
            }
            return StockingMode.DISABLED;
        }

        public void setStockingMode(StockingMode mode) {
            attributes[1] = (byte) mode.id;
        }
    }

    public String getId() { return id.toString(); }

    public String getTypeUID() { return CookTasks.getUID(type); }

    public int getRemain() { return remain; }

    public int getRequested() { return requested; }

    public List<Long> getTargets() { return Arrays.stream(targets).boxed().collect(Collectors.toList()); }

    public List<Byte> getAttributes() {
        return EncodeUtils.toList(attributes.getAttributes());
    }

    public static final Codec<CookRequest> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.STRING.fieldOf("id").forGetter(CookRequest::getId),
                Codec.STRING.fieldOf("type").forGetter(CookRequest::getTypeUID),
                Codec.INT.fieldOf("remain").forGetter(CookRequest::getRemain),
                Codec.INT.fieldOf("requested").forGetter(CookRequest::getRequested),
                Codec.LONG.listOf().fieldOf("targets").forGetter(CookRequest::getTargets),
                Codec.BYTE.listOf().fieldOf("attributes").forGetter(CookRequest::getAttributes)
        ).apply(instance,CookRequest::new)
    );

    public static final StreamCodec<FriendlyByteBuf, CookRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    CookRequest::getId,
                    ByteBufCodecs.STRING_UTF8,
                    CookRequest::getTypeUID,
                    ByteBufCodecs.INT,
                    CookRequest::getRemain,
                    ByteBufCodecs.INT,
                    CookRequest::getRequested,
                    ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()),
                    CookRequest::getTargets,
                    ByteBufCodecs.BYTE.apply(ByteBufCodecs.list()),
                    CookRequest::getAttributes,
                    CookRequest::new
            );
}
