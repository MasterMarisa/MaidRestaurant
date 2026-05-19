package com.mastermarisa.maid_restaurant.datagen.tag;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.tag.TagMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TagBlock extends BlockTagsProvider {
    public TagBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TagMod.STORAGE_BLOCK)
                .add(Blocks.CHEST)
                .add(Blocks.BARREL)
                .addOptional(new ResourceLocation("create:depot"))
                .addOptional(new ResourceLocation("create:item_vault"))
                .addOptional(new ResourceLocation("create:weighted_ejector"));
    }
}

