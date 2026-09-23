package com.mastermarisa.maid_restaurant.client.render.block;

import com.mastermarisa.maid_restaurant.block.OrderBellBlock;
import com.mastermarisa.maid_restaurant.blockentity.OrderBellBlockEntity;
import com.mastermarisa.maid_restaurant.client.animation.OrderBellAnimation;
import com.mastermarisa.maid_restaurant.client.model.OrderBellModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class OrderBellBlockEntityRender implements BlockEntityRenderer<OrderBellBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("maid_restaurant", "textures/block/order_bell.png");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private final OrderBellModel model;

    public OrderBellBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.model = new OrderBellModel(context.bakeLayer(OrderBellModel.LAYER_LOCATION));
    }

    @Override
    public void render(OrderBellBlockEntity bell, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = bell.getLevel();
        if (level != null) {
            Direction facing = bell.getBlockState().getValue(OrderBellBlock.FACING);
            int facingDeg = facing.get2DDataValue() * 90;
            float ageInTicks = (float) bell.getLevel().getGameTime() + partialTick;
            this.model.root().getAllParts().forEach(ModelPart::resetPose);
            bell.shakingState.updateTime(ageInTicks, 1.0F);
            bell.shakingState.ifStarted((state) -> KeyframeAnimations.animate(this.model, OrderBellAnimation.SHAKE, state.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE));
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, (double)0.5F);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YN.rotationDegrees((float)(180 - facingDeg)));
            VertexConsumer checkerBoardBuff = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            this.model.renderToBuffer(poseStack, checkerBoardBuff, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}
