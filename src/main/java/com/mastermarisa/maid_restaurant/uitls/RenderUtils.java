package com.mastermarisa.maid_restaurant.uitls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {
    public static void renderThickAABB(PoseStack poseStack, VertexConsumer consumer, AABB aabb,
                                       float thickness, float[] color) {
        renderThickAABB(poseStack, consumer, aabb, thickness, color[0], color[1], color[2], color[3]);
    }

    public static void renderThickAABB(PoseStack poseStack, VertexConsumer consumer, AABB aabb,
                                       float thickness, float r, float g, float b, float a) {

        float t = thickness / 2.0F;

        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        renderFilledBox(consumer, matrix, normal, minX - t, maxX + t, minY - t, minY + t, minZ - t, minZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, minX - t, maxX + t, minY - t, minY + t, maxZ - t, maxZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, minX - t, maxX + t, maxY - t, maxY + t, minZ - t, minZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, minX - t, maxX + t, maxY - t, maxY + t, maxZ - t, maxZ + t, r, g, b, a);

        renderFilledBox(consumer, matrix, normal, minX - t, minX + t, minY - t, maxY + t, minZ - t, minZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, minX - t, minX + t, minY - t, maxY + t, maxZ - t, maxZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, maxX - t, maxX + t, minY - t, maxY + t, minZ - t, minZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, maxX - t, maxX + t, minY - t, maxY + t, maxZ - t, maxZ + t, r, g, b, a);

        renderFilledBox(consumer, matrix, normal, minX - t, minX + t, minY - t, minY + t, minZ - t, maxZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, minX - t, minX + t, maxY - t, maxY + t, minZ - t, maxZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, maxX - t, maxX + t, minY - t, minY + t, minZ - t, maxZ + t, r, g, b, a);
        renderFilledBox(consumer, matrix, normal, maxX - t, maxX + t, maxY - t, maxY + t, minZ - t, maxZ + t, r, g, b, a);
    }

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    private static void renderFilledBox(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                         double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                                         float red, float green, float blue, float alpha) {
        float x0 = (float) minX;
        float x1 = (float) maxX;
        float y0 = (float) minY;
        float y1 = (float) maxY;
        float z0 = (float) minZ;
        float z1 = (float) maxZ;

        consumer.vertex(matrix, x0, y0, z0).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y0, z0).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y0, z1).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x0, y0, z1).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();

        consumer.vertex(matrix, x0, y1, z1).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z0).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x0, y1, z0).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();

        consumer.vertex(matrix, x0, y0, z0).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x0, y1, z0).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z0).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x1, y0, z0).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();

        consumer.vertex(matrix, x1, y0, z1).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, x0, y1, z1).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, x0, y0, z1).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();

        consumer.vertex(matrix, x0, y0, z1).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x0, y1, z1).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x0, y1, z0).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x0, y0, z0).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();

        consumer.vertex(matrix, x1, y0, z0).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z0).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(matrix, x1, y0, z1).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
    }
}
