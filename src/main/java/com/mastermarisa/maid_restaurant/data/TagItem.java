package com.mastermarisa.maid_restaurant.data;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.InitItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class TagItem extends ItemTagsProvider {
    public static final TagKey<Item> TABLE_BLACKLIST = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MaidRestaurant.MOD_ID, "table_blacklist"));

    public TagItem(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                   CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TABLE_BLACKLIST)
                .add(InitItems.ORDER_MENU.get());
    }
}
