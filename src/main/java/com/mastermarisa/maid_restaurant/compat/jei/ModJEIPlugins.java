package com.mastermarisa.maid_restaurant.compat.jei;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ModJEIPlugins implements IModPlugin {
    private static final ResourceLocation UID = MaidRestaurant.modLoc("jei");

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, MenuTransformingRecipeMaker.createRecipes());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
