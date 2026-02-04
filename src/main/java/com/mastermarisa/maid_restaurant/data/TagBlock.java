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
    public static final TagKey<Block> STORAGE_BLOCK = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MaidRestaurant.MOD_ID, "storage_block"));
    public static final TagKey<Block> SIT_BLOCK = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MaidRestaurant.MOD_ID, "sit_block"));
    public static final TagKey<Block> SERVE_MEAL_BLOCK = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MaidRestaurant.MOD_ID, "serve_blockmeal_block"));

    public TagBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider){
        tag(STORAGE_BLOCK)
                .add(Blocks.CHEST);

        getOrCreateRawBuilder(SIT_BLOCK)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"cook_stool"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"chair"));

        getOrCreateRawBuilder(SERVE_MEAL_BLOCK)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"table"));
    }

    @Override
    public @NotNull String getName() {
        return "None.";
    }
}
