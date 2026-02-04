package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmptyRenderer<T extends Entity> extends EntityRenderer<T> {
    public EmptyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return MaidRestaurant.resourceLocation("");
    }

    @Override
    public boolean shouldRender(T entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {

    }
}
