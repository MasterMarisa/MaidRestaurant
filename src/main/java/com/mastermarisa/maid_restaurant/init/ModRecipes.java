package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.crafting.serializer.MenuTransformingRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MaidRestaurant.MOD_ID);
    public static RegistryObject<RecipeSerializer<?>> MENU_TRANSFORMING_SERIALIZER = RECIPE_SERIALIZERS.register("menu_transforming", MenuTransformingRecipeSerializer::new);
}
