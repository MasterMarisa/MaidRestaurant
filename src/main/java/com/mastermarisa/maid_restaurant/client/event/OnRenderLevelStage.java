package com.mastermarisa.maid_restaurant.client.event;

import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.render.WorldItemRenderer;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.utils.component.BlockSelection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
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
        Player player = Minecraft.getInstance().player;

        assert player != null;
        if (player.getMainHandItem().is(ModItems.ORDER_MENU)) {
            for (BlockPos pos : player.getData(BlockSelection.TYPE).menu) {
                WorldItemRenderer.renderItemStackTowardPlayer(
                        poseStack,
                        bufferSource,
                        pos.above().getCenter(),
                        Minecraft.getInstance(),
                        new ItemStack(ModItems.ORDER_MENU.get()),
                        LightTexture.FULL_SKY
                );
            }
        }

        if (player.getMainHandItem().is(ModItems.ORDER_ITEM)) {
            for (BlockPos pos : player.getData(BlockSelection.TYPE).order) {
                WorldItemRenderer.renderItemStackTowardPlayer(
                        poseStack,
                        bufferSource,
                        pos.above().getCenter(),
                        Minecraft.getInstance(),
                        new ItemStack(ModItems.ORDER_ITEM.get()),
                        LightTexture.FULL_SKY
                );
            }
        }
    }
}
