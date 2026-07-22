package com.mastermarisa.maid_restaurant.data.request;

import com.google.gson.JsonElement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServeRequest implements INBTSerializable<CompoundTag> {
    private static final String TAG_DISH = "dish";
    private static final String TAG_COUNT = "count";
    private static final String TAG_PICKUP_POINTS = "pickup_points";
    private static final String TAG_TARGETS = "targets";

    public Ingredient dish;
    public int count;
    public List<BlockPos> pickupPoints;
    public List<BlockPos> targets;

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
            tag.putLongArray(TAG_PICKUP_POINTS, this.pickupPoints.stream().mapToLong(BlockPos::asLong).toArray());
        }
        if (!this.targets.isEmpty()) {
            tag.putLongArray(TAG_TARGETS, this.targets.stream().mapToLong(BlockPos::asLong).toArray());
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
            this.pickupPoints = Arrays.stream(tag.getLongArray(TAG_PICKUP_POINTS))
                    .mapToObj(BlockPos::of)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (tag.contains(TAG_TARGETS)) {
            this.targets = Arrays.stream(tag.getLongArray(TAG_TARGETS))
                    .mapToObj(BlockPos::of)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static ServeRequest fromNBT(CompoundTag tag) {
        ServeRequest request = new ServeRequest();
        request.deserializeNBT(tag);
        return request;
    }
}
