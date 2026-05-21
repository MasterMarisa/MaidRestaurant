package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.core.zone.CuboidZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CuboidZoneDefinitionItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CuboidZoneRenderer {
    private static final float[] GRAN_BLUE = new float[]{ 139 / 255F, 193 / 255F, 250 / 255F, 1};
    private static final float ZONE_TRANSITION_SPEED = 0.2f;
    private static Vec3 cuboidZoneCache = null;

    public static void renderCuboidZoneSelection(PoseStack poseStack, VertexConsumer consumer,
                                                 Vec3 cameraOffset, @Nullable BlockPos hoveredBlock) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack definition;
        if (player.getMainHandItem().is(ModItems.CUBOID_ZONE_DEFINITION.get())) {
            definition = player.getMainHandItem();
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CUBOID_ZONE_DEFINITION.get())) {
            definition = player.getItemInHand(InteractionHand.OFF_HAND);
        } else {
            return;
        }

        BlockPos point1 = CuboidZoneDefinitionItem.getTempVertex(definition);
        if (point1 != null) {
            AABB aabb;
            if (hoveredBlock == null) {
                aabb = new AABB(point1);
            } else {
                Vec3 vector1 = point1.getCenter();
                Vec3 vector2;
                if (cuboidZoneCache == null) {
                    vector2 = hoveredBlock.getCenter();
                } else {
                    Vec3 center = hoveredBlock.getCenter();
                    float x = lerp((float) cuboidZoneCache.x, (float) center.x, ZONE_TRANSITION_SPEED);
                    float y = lerp((float) cuboidZoneCache.y, (float) center.y, ZONE_TRANSITION_SPEED);
                    float z = lerp((float) cuboidZoneCache.z, (float) center.z, ZONE_TRANSITION_SPEED);
                    vector2 = new Vec3(x, y, z);
                }
                cuboidZoneCache = vector2;
                aabb = new AABB(
                        Math.min(vector1.x, vector2.x) - 0.5,
                        Math.min(vector1.y, vector2.y) - 0.5,
                        Math.min(vector1.z, vector2.z) - 0.5,
                        Math.max(vector1.x, vector2.x) + 0.5,
                        Math.max(vector1.y, vector2.y) + 0.5,
                        Math.max(vector1.z, vector2.z) + 0.5
                );
            }
            RenderUtils.renderThickAABB(
                    poseStack,
                    consumer,
                    aabb.move(cameraOffset),
                    0.075F,
                    GRAN_BLUE
            );
            return;
        }

        CuboidZone zone = CuboidZoneDefinitionItem.getZone(definition);
        if (zone != null) {
            AABB aabb = fromTo(zone.getMin(), zone.getMax());
            RenderUtils.renderThickAABB(
                    poseStack,
                    consumer,
                    aabb.move(cameraOffset),
                    0.075F,
                    GRAN_BLUE
            );
        }
    }

    private static AABB fromTo(BlockPos from, BlockPos to) {
        return new AABB(
                Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()),
                Math.min(from.getZ(), to.getZ()),
                Math.max(from.getX(), to.getX()) + 1,
                Math.max(from.getY(), to.getY()) + 1,
                Math.max(from.getZ(), to.getZ()) + 1
        );
    }

    private static float lerp(float a, float b, float amount) {
        return a + (b - a) * amount;
    }
}
