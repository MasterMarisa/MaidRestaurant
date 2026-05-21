package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.core.zone.CuboidZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CuboidZoneDefinitionItem;
import com.mastermarisa.maid_restaurant.uitls.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CuboidZoneRenderer {
    private static final float[] GRAN_BLUE = new float[]{ 139 / 255F, 193 / 255F, 250 / 255F, 1};
    private static final float ZONE_TRANSITION_SPEED = 0.2f;
    private static final float SELECTOR_TRANSITION_SPEED = 0.2f;
    private static final Vec3CacheHolder zoneCache = new Vec3CacheHolder();
    private static final Vec3CacheHolder selectorCache = new Vec3CacheHolder();

    public static void render(PoseStack poseStack, VertexConsumer consumer,
                              Vec3 cameraOffset, @Nullable BlockPos targetedBlockCache) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack definition = player.getMainHandItem();
        if (!definition.is(ModItems.CUBOID_ZONE_DEFINITION.get())) {
            return;
        }

        BlockPos tempVertex = CuboidZoneDefinitionItem.getTempVertex(definition);
        if (tempVertex != null) {
            Vec3 vector1 = tempVertex.getCenter();
            Vec3 vector2;
            if (targetedBlockCache == null) {
                vector2 = vector1;
            } else {
                vector2 = zoneCache.lerpTo(targetedBlockCache.getCenter(), ZONE_TRANSITION_SPEED);
            }
            AABB aabb = new AABB(
                    Math.min(vector1.x, vector2.x) - 0.5,
                    Math.min(vector1.y, vector2.y) - 0.5,
                    Math.min(vector1.z, vector2.z) - 0.5,
                    Math.max(vector1.x, vector2.x) + 0.5,
                    Math.max(vector1.y, vector2.y) + 0.5,
                    Math.max(vector1.z, vector2.z) + 0.5
            );
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
            return;
        }

        if (targetedBlockCache != null) {
            Vec3 selector = selectorCache.lerpTo(targetedBlockCache.getCenter(), SELECTOR_TRANSITION_SPEED);
            AABB aabb = new AABB(
                    selector.x - 0.5,
                    selector.y - 0.5,
                    selector.z - 0.5,
                    selector.x + 0.5,
                    selector.y + 0.5,
                    selector.z + 0.5
            );
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
}
