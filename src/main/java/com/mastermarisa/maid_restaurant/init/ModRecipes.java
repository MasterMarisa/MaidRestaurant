package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.crafting.recipe.MenuTransformingRecipe;
import com.mastermarisa.maid_restaurant.crafting.serializer.MenuTransformingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MaidRestaurant.MOD_ID);

    public static RegistryObject<RecipeSerializer<?>> MENU_TRANSFORMING_SERIALIZER = RECIPE_SERIALIZERS.register("menu_transforming", MenuTransformingRecipeSerializer::new);

    public static RecipeType<MenuTransformingRecipe> MENU_TRANSFORMING_RECIPE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            MENU_TRANSFORMING_RECIPE = RecipeType.simple(MaidRestaurant.modLoc("menu_transforming"));
        }
    }
}
