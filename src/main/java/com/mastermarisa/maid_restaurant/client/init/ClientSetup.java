package com.mastermarisa.maid_restaurant.client.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.screen.ChefLicenseScreen;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.inventory.container.ChefLicenseContainer;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ChefLicenseContainer.TYPE, ChefLicenseScreen::new));
        event.enqueueWork(() -> ItemProperties.register(ModItems.COOKING_GUIDE.get(), CookingGuideItem.HAS_RECIPE_PROPERTY, CookingGuideItem::getTexture));
    }
}
