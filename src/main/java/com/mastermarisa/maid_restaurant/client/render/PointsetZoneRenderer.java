package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.core.zone.PointsetZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.PointsetZoneDefinitionItem;
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
public class PointsetZoneRenderer {
    private static final float[] GRAN_BLUE = new float[]{ 139 / 255F, 193 / 255F, 250 / 255F, 1};
    private static final float SELECTOR_TRANSITION_SPEED = 0.2f;
    private static final Vec3CacheHolder selectorCache = new Vec3CacheHolder();

    public static void render(PoseStack poseStack, VertexConsumer consumer,
                              Vec3 cameraOffset, @Nullable BlockPos targetedBlockCache) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack definition = player.getMainHandItem();
        if (!definition.is(ModItems.POINTSET_ZONE_DEFINITION.get())) {
            return;
        }

        PointsetZone zone = PointsetZoneDefinitionItem.getZone(definition);
        if (zone != null) {
            for (BlockPos pos : zone) {
                RenderUtils.renderThickAABB(
                        poseStack,
                        consumer,
                        new AABB(pos).move(cameraOffset),
                        0.075F,
                        GRAN_BLUE
                );
            }
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
}
