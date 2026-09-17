package com.mastermarisa.maid_restaurant.datagen.model;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.COOKING_GUIDE.get());
        basicItem(ModItems.CHEF_LICENSE.get());
        basicItem(ModItems.CUBOID_ZONE_DEFINITION.get());
        basicItem(ModItems.POINTSET_ZONE_DEFINITION.get());
        basicItem(ModItems.UNBOUND_MENU.get());
    }
}
