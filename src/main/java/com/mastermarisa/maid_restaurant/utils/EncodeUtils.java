package com.mastermarisa.maid_restaurant.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncodeUtils {
    public static ResourceLocation encode(Item item){
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation encode(ItemStack stack){
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static Item decode(String key){ return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key)); }

    public static Item decode(ResourceLocation key){
        return BuiltInRegistries.ITEM.get(key);
    }

    public static List<BlockPos> decode(long[] packed) {
        return Arrays.stream(packed).mapToObj(EncodeUtils::decode).toList();
    }

    public static BlockPos decode(long packed) {
        return new BlockPos(BlockPos.getX(packed),BlockPos.getY(packed),BlockPos.getZ(packed));
    }

    public static long[] encode(List<BlockPos> pos) {
        long[] packed = new long[pos.size()];
        for (int i = 0;i < pos.size();i++){
            packed[i] = pos.get(i).asLong();
        }

        return packed;
    }

    public static List<Byte> toList(byte[] byteArray) {
        List<Byte> bytes = new ArrayList<>();
        for (byte b : byteArray) bytes.add(b);
        return bytes;
    }

    public static byte[] toArray(List<Byte> byteList) {
        byte[] array = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            array[i] = byteList.get(i);
        }
        return array;
    }
}
