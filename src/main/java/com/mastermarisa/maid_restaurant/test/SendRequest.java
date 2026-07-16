package com.mastermarisa.maid_restaurant.test;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class SendRequest {
    @SubscribeEvent
    public static void onPlayerJoinLevel(EntityJoinLevelEvent event) {
//        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Player player
//                && event.getLevel() instanceof ServerLevel level) {
//            RecipeNode planks = new RecipeNode(Ingredient.of(ItemTags.PLANKS), 3, null);
//            RecipeNode stone = new RecipeNode(Ingredient.of(Items.COBBLESTONE), 4, null);
//            RecipeNode iron = new RecipeNode(Ingredient.of(Items.IRON_INGOT), 1, null);
//            RecipeNode redStone = new RecipeNode(Ingredient.of(Items.REDSTONE), 1, null);
//
//            RecipeNode piston = new RecipeNode(Ingredient.of(Items.PISTON), 1, null);
//            RecipeNode slime_ball = new RecipeNode(Ingredient.of(Items.SLIME_BALL), 1, null);
//
//            RecipeNode sticky_piston = new RecipeNode(Ingredient.of(Items.STICKY_PISTON), 8, null);
//
//            piston.setCombineStep(new RecipeStep(CraftingTableCapability.UID, new ResourceLocation("minecraft:piston")));
//            piston.setChildren(List.of(planks, stone, iron, redStone));
//
//            sticky_piston.setCombineStep(new RecipeStep(CraftingTableCapability.UID, new ResourceLocation("minecraft:sticky_piston")));
//            sticky_piston.setChildren(List.of(slime_ball, piston));
//
//            CookingRequest request = new CookingRequest(sticky_piston);
//            CookingRequestBus.get(level).add("maid_restaurant", request);
//
//            ItemStack cookingGuide = ModItems.COOKING_GUIDE.get().getDefaultInstance();
//            CookingGuideItem.setRecipeRoot(cookingGuide, sticky_piston.serializeNBT());
//            ItemHandlerHelper.giveItemToPlayer(player, cookingGuide);
//        }
    }
}
