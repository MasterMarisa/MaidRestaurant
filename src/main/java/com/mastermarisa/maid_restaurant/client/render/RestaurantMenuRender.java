package com.mastermarisa.maid_restaurant.client.render;

import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RestaurantMenuRender {
    private static final float[] BLUE = new float[]{ 139 / 255F, 193 / 255F, 250 / 255F, 1};
    private static final float[] ORANGE = new float[]{ 221 / 255F, 120 / 255F, 41 / 255F, 1};
    private static final float SELECTOR_TRANSITION_SPEED = 0.2f;
    private static final Vec3CacheHolder SELECTOR_CACHE = new Vec3CacheHolder();

    public static void render(PoseStack poseStack, VertexConsumer consumer,
                              Vec3 cameraOffset, @Nullable BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack itemInHand = player.getMainHandItem();
        if (!itemInHand.is(ModItems.RESTAURANT_MENU.get()) || !RestaurantMenuItem.isSelectingTargets(itemInHand)) {
            return;
        }

        List<ServeRequest.Target> targets = RestaurantMenuItem.getTargets(itemInHand);
        for (ServeRequest.Target target : targets) {
            RenderUtil.renderThickAABB(
                    poseStack,
                    consumer,
                    new AABB(target.pos()).move(cameraOffset),
                    0.075F,
                    target.type() == 0 ? BLUE : ORANGE
            );
        }

        if (pos != null) {
            Level level = mc.level;
            int type = level != null && StorageRegistry.tryGetAt(level, pos) != null ? 0 : 1;
            Vec3 selector = SELECTOR_CACHE.lerpTo(pos.getCenter(), SELECTOR_TRANSITION_SPEED);
            AABB aabb = new AABB(
                    selector.x - 0.5,
                    selector.y - 0.5,
                    selector.z - 0.5,
                    selector.x + 0.5,
                    selector.y + 0.5,
                    selector.z + 0.5
            );
            RenderUtil.renderThickAABB(
                    poseStack,
                    consumer,
                    aabb.move(cameraOffset),
                    0.075F,
                    type == 0 ? BLUE : ORANGE
            );
        }
    }
}
