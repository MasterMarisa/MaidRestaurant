package com.mastermarisa.maid_restaurant.datagen.model;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockModelGenerator extends BlockModelProvider {
    public BlockModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        cubeAll("creative_crate", modLoc("block/creative_crate"));
    }
}
