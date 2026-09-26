package com.mastermarisa.maid_restaurant.crafting.serializer;

import com.google.gson.JsonObject;
import com.mastermarisa.maid_restaurant.crafting.recipe.MenuTransformingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class MenuTransformingRecipeSerializer extends ShapelessRecipe.Serializer {
    @Override
    public MenuTransformingRecipe fromJson(ResourceLocation id, JsonObject json) {
        ShapelessRecipe base = super.fromJson(id, json);
        return new MenuTransformingRecipe(
                id,
                base.category(),
                base.getResultItem(RegistryAccess.EMPTY),
                base.getIngredients()
        );
    }

    @Override
    public MenuTransformingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ShapelessRecipe base = super.fromNetwork(id, buf);
        return new MenuTransformingRecipe(
                id,
                base.category(),
                base.getResultItem(RegistryAccess.EMPTY),
                base.getIngredients()
        );
    }
}
