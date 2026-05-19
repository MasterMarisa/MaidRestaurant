package com.mastermarisa.maid_restaurant.test;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.capability.CraftingTableCapability;
import com.mastermarisa.maid_restaurant.core.recipe.CookStep;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeNode;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class GiveCookingGuide {
    @SubscribeEvent
    public static void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Player player) {
            RecipeNode planks = new RecipeNode(Ingredient.of(ItemTags.PLANKS), 3, null);
            RecipeNode stone = new RecipeNode(Ingredient.of(Items.COBBLESTONE), 4, null);
            RecipeNode iron = new RecipeNode(Ingredient.of(Items.IRON_INGOT), 1, null);
            RecipeNode redStone = new RecipeNode(Ingredient.of(Items.REDSTONE), 1, null);

            RecipeNode piston = new RecipeNode(Ingredient.of(Items.PISTON), 1, null);
            RecipeNode slime_ball = new RecipeNode(Ingredient.of(Items.SLIME_BALL), 1, null);

            RecipeNode sticky_piston = new RecipeNode(Ingredient.of(Items.STICKY_PISTON), 8, null);

            piston.setCombineStep(new CookStep(CraftingTableCapability.UID, tagWithRecipeId("minecraft:piston")));
            piston.setChildren(List.of(planks, stone, iron, redStone));

            sticky_piston.setCombineStep(new CookStep(CraftingTableCapability.UID, tagWithRecipeId("minecraft:sticky_piston")));
            sticky_piston.setChildren(List.of(slime_ball, piston));

            ItemStack cookingGuide = ModItems.COOKING_GUIDE.get().getDefaultInstance();
            CookingGuideItem.setRecipeRoot(cookingGuide, sticky_piston.serializeNBT());
            ItemHandlerHelper.giveItemToPlayer(player, cookingGuide);
        }
    }

    private static CompoundTag tagWithRecipeId(String recipeId) {
        CompoundTag params = new CompoundTag();
        params.putString(CookStep.RECIPE_ID, recipeId);
        return params;
    }
}
