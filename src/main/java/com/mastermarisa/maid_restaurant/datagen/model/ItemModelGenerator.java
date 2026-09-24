package com.mastermarisa.maid_restaurant.datagen.model;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MaidRestaurant.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.CHEF_LICENSE.get());
        basicItem(ModItems.WAITER_LICENSE.get());
        basicItem(ModItems.CUBOID_ZONE_DEFINITION.get());
        basicItem(ModItems.POINTSET_ZONE_DEFINITION.get());
        basicItem(ModItems.UNBOUND_MENU.get());
        basicItem(ModItems.RESTAURANT_MENU.get());

        withExistingParent("creative_crate", modLoc("block/creative_crate"));

        ResourceLocation cookingGuide = ForgeRegistries.ITEMS.getKey(ModItems.COOKING_GUIDE.get());
        if (cookingGuide != null) {
            ItemModelBuilder noRecipe = this.basicItem(MaidRestaurant.modLoc("cooking_guide_no_recipe"));
            ItemModelBuilder hasRecipe = this.basicItem(MaidRestaurant.modLoc("cooking_guide_has_recipe"));
            this.getBuilder(cookingGuide.toString())
                    .override().model(noRecipe).predicate(CookingGuideItem.HAS_RECIPE_PROPERTY, 0.0F).end()
                    .override().model(hasRecipe).predicate(CookingGuideItem.HAS_RECIPE_PROPERTY, 1.0F).end();
        }
    }
}
