package com.mastermarisa.maid_restaurant.uitls;

import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class ClientUtil {
    @Nullable
    public static ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Nullable
    public static BlockPos getTargetedBlock() {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType().equals(HitResult.Type.BLOCK) && hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            Direction direction = hit.getDirection();
            if (direction == Direction.UP || direction == Direction.DOWN) {
                pos = pos.relative(direction);
            }
            return pos;
        }
        return null;
    }

    public static int getScreenCenterX(){
        return Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;
    }

    public static void renderIngredientStack(GuiGraphics graphics, int x, int y,
                                             IngredientStack stack, long gameTime, int interval) {
        ItemStack[] items = stack.getItems();
        int index = Math.toIntExact(gameTime / interval) % items.length;
        ItemStack itemStack = items[index].copyWithCount(1);
        Font font = Minecraft.getInstance().font;
        drawCenteredString(graphics, font, "x" + stack.getCount(), x + 16, y + 13, 200, 0.6F, Color.WHITE.getRGB());
        graphics.renderFakeItem(itemStack, x, y);
        graphics.renderItemDecorations(font, itemStack, x, y);
    }

    public static void renderIngredient(GuiGraphics graphics, int x, int y,
                                        Ingredient stack, long gameTime, int interval) {
        ItemStack[] items = stack.getItems();
        int index = Math.toIntExact(gameTime / interval) % items.length;
        Font font = Minecraft.getInstance().font;
        graphics.renderFakeItem(items[index], x, y);
        graphics.renderItemDecorations(font, items[index], x, y);
    }

    public static void drawCenteredString(GuiGraphics graphics, Font font, String text,
                                          int x, int y, int z, float scale, int color) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        {
            pose.translate(x, y, z);
            pose.scale(scale, scale, 1);
            graphics.drawCenteredString(font, text, 0, 0, color);
        }
        pose.popPose();
    }
}
