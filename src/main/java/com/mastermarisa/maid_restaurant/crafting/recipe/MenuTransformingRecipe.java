package com.mastermarisa.maid_restaurant.crafting.recipe;

import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.init.ModRecipes;
import com.mastermarisa.maid_restaurant.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.item.PortableMenuItem;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class MenuTransformingRecipe extends ShapelessRecipe {
    public MenuTransformingRecipe(ResourceLocation id, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, "", category, result, ingredients);
    }

    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack itemstack = findMenuItem(container);
        ItemStack result = this.getResultItem(registryAccess).copy();
        if (!itemstack.isEmpty()) {
            UnboundMenuItem.setEntries(result, UnboundMenuItem.getEntries(itemstack));
            if (!result.is(ModItems.UNBOUND_MENU.get())) {
                PortableMenuItem.setRestaurantId(result, PortableMenuItem.getRestaurantId(itemstack));
            }
         }
        return result;
    }

    private static ItemStack findMenuItem(CraftingContainer container) {
        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemstack = container.getItem(i);
            if (itemstack.is(TagMod.MENU_ITEM)) {
                return itemstack;
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MENU_TRANSFORMING_SERIALIZER.get();
    }
}
