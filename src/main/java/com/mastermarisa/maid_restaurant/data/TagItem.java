package com.mastermarisa.maid_restaurant.data;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
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

    public static final TagKey<Item> BASKET_BLACKLIST = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MaidRestaurant.MOD_ID, "basket_blacklist"));

    public static final TagKey<Item> BRAISED_FISH_INGREDIENT = ItemTags.create(MaidRestaurant.resourceLocation("braised_fish_ingredient"));

    public TagItem(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                   CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TABLE_BLACKLIST)
                .add(ModItems.ORDER_MENU.get())
                .add(ModItems.ORDER_ITEM.get());

        tag(BASKET_BLACKLIST)
                .add(ModItems.ORDER_MENU.get())
                .add(ModItems.ORDER_ITEM.get());

        tag(BRAISED_FISH_INGREDIENT)
                .addOptionalTag(ResourceLocation.parse("c:foods/raw_cod"))
                .addOptionalTag(ResourceLocation.parse("c:foods/raw_salmon"));
    }
}
