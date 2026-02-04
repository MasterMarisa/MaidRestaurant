package com.mastermarisa.maid_restaurant.client.events;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.render.WorldItemRenderer;
import com.mastermarisa.maid_restaurant.entity.attachment.BlockSelection;
import com.mastermarisa.maid_restaurant.init.InitItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MaidRestaurant.MOD_ID,value = Dist.CLIENT)
public class OnRenderLevelStage {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        assert Minecraft.getInstance().player != null;
        if (!Minecraft.getInstance().player.getMainHandItem().is(InitItems.ORDER_MENU)) return;
        for (BlockPos pos : Minecraft.getInstance().player.getData(BlockSelection.TYPE).selected) {
            WorldItemRenderer.renderItemStackTowardPlayer(
                    poseStack,
                    bufferSource,
                    pos.above().getCenter(),
                    Minecraft.getInstance(),
                    new ItemStack(InitItems.ORDER_MENU.get()),
                    LightTexture.FULL_SKY
            );
        }
    }
}
