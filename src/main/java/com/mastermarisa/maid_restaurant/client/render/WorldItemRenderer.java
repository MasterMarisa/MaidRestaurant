package com.mastermarisa.maid_restaurant.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Vector3f;

public class WorldItemRenderer {
    public static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    public static void renderItemStackTowardPlayer(PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 targetPos
            , Minecraft minecraft, ItemStack itemStack, int packedLight) {
        boolean shader = IRIS_LOADED && ShaderState.shaderEnabled();

        Vector3f shaderLightDirections$1 = null;
        Vector3f shaderLightDirections$2 = null;
        if (!shader){
            if (buffer instanceof MultiBufferSource.BufferSource bufferSource)
                bufferSource.endBatch();
            shaderLightDirections$1 = new Vector3f(RenderSystem.shaderLightDirections[0]);
            shaderLightDirections$2 = new Vector3f(RenderSystem.shaderLightDirections[1]);
        }

        poseStack.pushPose();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        Vec3 renderPos = targetPos.add(0, 0.5, 0).subtract(cameraPos);

        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);

        double dx = cameraPos.x - (targetPos.x + 0.5);
        double dz = cameraPos.z - (targetPos.z + 0.5);
        float yaw = (float) Math.atan2(dz, dx);

        poseStack.mulPose(Axis.YP.rotation(-yaw + (float) Math.PI / 2));

        float time = (minecraft.level.getGameTime()) / 20.0f;
        poseStack.translate(0, Math.sin(time) * 0.1, 0);

        //poseStack.mulPose(Axis.YP.rotation(time % 360));

        poseStack.scale(0.5f, 0.5f, 0.5f);

        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        if (!shader){
            Vector3f vec = poseStack.last().pose().transformDirection(new Vector3f(0, 0, 1)).normalize();
            RenderSystem.setShaderLights(vec, vec);
            packedLight = LightTexture.FULL_BRIGHT;
        }

        itemRenderer.renderStatic(
                itemStack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                minecraft.level,
                0
        );

        poseStack.popPose();

        if (!shader){
            if (buffer instanceof MultiBufferSource.BufferSource bufferSource)
                bufferSource.endBatch();
            RenderSystem.shaderLightDirections[0] = shaderLightDirections$1;
            RenderSystem.shaderLightDirections[1] = shaderLightDirections$2;
        }
    }
}
