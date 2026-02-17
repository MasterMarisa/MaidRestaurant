package com.mastermarisa.maid_restaurant.data;

import com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TagBlock extends BlockTagsProvider {
    public static final TagKey<Block> STORAGE_BLOCK = BlockTags.create(MaidRestaurant.resourceLocation("storage_block"));
    public static final TagKey<Block> SIT_BLOCK = BlockTags.create(MaidRestaurant.resourceLocation("sit_block"));
    public static final TagKey<Block> SERVE_MEAL_BLOCK = BlockTags.create(MaidRestaurant.resourceLocation("serve_blockmeal_block"));

    public TagBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider){
        tag(STORAGE_BLOCK)
                .add(Blocks.CHEST)
                .add(Blocks.BARREL);

        getOrCreateRawBuilder(SIT_BLOCK)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"cook_stool"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"chair"));

        getOrCreateRawBuilder(SERVE_MEAL_BLOCK)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"table"))
                .addOptionalElement(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"fruit_basket"));
    }

    @Override
    public @NotNull String getName() {
        return MaidRestaurant.MOD_ID + "_Block";
    }
}
