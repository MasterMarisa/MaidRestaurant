package com.mastermarisa.maid_restaurant.data.request;

import com.google.gson.JsonElement;
import com.mastermarisa.maid_restaurant.uitls.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class ServeRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_DISH = "dish";
    private static final String TAG_COUNT = "count";
    private static final String TAG_PICKUP_POINTS = "pickup_points";
    private static final String TAG_TARGETS = "targets";

    public Ingredient dish;
    public int count;
    public List<Source> pickupPoints;
    public List<Target> targets;

    public ServeRequest() {
        this.pickupPoints = new ArrayList<>();
        this.targets = new ArrayList<>();
    }

    public ServeRequest(Ingredient dish, int count) {
        this.dish = dish;
        this.count = count;
        this.pickupPoints = new ArrayList<>();
        this.targets = new ArrayList<>();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!this.dish.isEmpty()) {
            tag.putString(TAG_DISH, this.dish.toJson().toString());
        }
        tag.putInt(TAG_COUNT, this.count);
        if (!this.pickupPoints.isEmpty()) {
            ListTag listTag = new ListTag();
            for (var source : pickupPoints) {
                listTag.add(CodecUtil.serialize(source, Source.CODEC));
            }
            tag.put(TAG_PICKUP_POINTS, listTag);
        }
        if (!this.targets.isEmpty()) {
            ListTag listTag = new ListTag();
            for (var target : targets) {
                listTag.add(CodecUtil.serialize(target, Target.CODEC));
            }
            tag.put(TAG_TARGETS, listTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TAG_DISH)) {
            JsonElement jsonElement = GsonHelper.parse(tag.getString(TAG_DISH));
            this.dish = Ingredient.fromJson(jsonElement);
        } else {
            this.dish = Ingredient.EMPTY;
        }
        if (tag.contains(TAG_COUNT)) {
            this.count = tag.getInt(TAG_COUNT);
        }
        if (tag.contains(TAG_PICKUP_POINTS)) {
            this.pickupPoints.clear();
            ListTag listTag = tag.getList(TAG_PICKUP_POINTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                this.pickupPoints.add(CodecUtil.deserialize(listTag.getCompound(i), Source.CODEC));
            }
        }
        if (tag.contains(TAG_TARGETS)) {
            this.targets.clear();
            ListTag listTag = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                this.targets.add(CodecUtil.deserialize(listTag.getCompound(i), Target.CODEC));
            }
        }
    }

    public static ServeRequest fromNBT(CompoundTag tag) {
        ServeRequest request = new ServeRequest();
        request.deserializeNBT(tag);
        return request;
    }

    public record Source(BlockPos pos, int count) {
        public static final Codec<Source> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(Source::pos),
                    Codec.INT.fieldOf("count").forGetter(Source::count)
            ).apply(instance, Source::new)
        );
    }

    public record Target(BlockPos pos, int type) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(Target::pos),
                    Codec.INT.fieldOf("type").forGetter(Target::type)
            ).apply(instance, Target::new)
        );
    }
}
