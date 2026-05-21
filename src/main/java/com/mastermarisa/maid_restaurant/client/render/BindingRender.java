package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID)
public class BindingRender {
    private static final ResourceLocation EMPTY_TEXTURE = MaidRestaurant.resourceLocation("textures/white.png");
    private static BlockPos hoveredBlock = null;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.level == null) {
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(EMPTY_TEXTURE));
            Vec3 position = event.getCamera().getPosition().reverse();

            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType().equals(HitResult.Type.BLOCK) && hitResult instanceof BlockHitResult hit) {
                hoveredBlock = hit.getBlockPos();
                Direction direction = hit.getDirection();
                if (direction == Direction.UP || direction == Direction.DOWN) {
                    hoveredBlock = hoveredBlock.relative(direction);
                }
            }

            CuboidZoneRenderer.renderCuboidZoneSelection(poseStack, consumer, position, hoveredBlock);
        }
    }


}
