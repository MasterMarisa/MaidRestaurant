package com.mastermarisa.maid_restaurant.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class UIScrollBox extends UIElement {
    protected float scrollOffset;

    public UIScrollBox(Rectangle frame) {
        super(frame);
    }

    public static class Y extends UIScrollBox {
        private final Alignment alignment;

        public Y(Rectangle frame, Alignment alignment) {
            super(frame);
            this.alignment = alignment;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            List<UIElement> toRender = new ArrayList<>();
            float v = collectElements(toRender);
            if (toRender.isEmpty()) {
                return;
            }

            graphics.enableScissor(this.getMinX(), this.getMinY(), this.getMaxX(), this.getMaxY());
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            {
                poseStack.translate(0, v, 0);
                for (var element : toRender) {
                    element.setMinY(this.frame.y);
                    switch (this.alignment) {
                        case LEFT -> element.setMinX(this.getMinX());
                        case CENTER -> element.setCenterX(this.getCenterX());
                        case RIGHT -> element.setMaxX(this.getMaxX());
                    }
                    element.render(graphics, mouseX, mouseY);
                    poseStack.translate(0, element.getHeight(), 0);
                }
            }
            poseStack.popPose();
            graphics.disableScissor();
        }

        @Override
        public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (this.frame.contains(mouseX, mouseY)) {
                int totalHeight = children.stream().mapToInt(UIElement::getHeight).sum();
                this.scrollOffset += (float) scrollY * 3;
                this.scrollOffset = Mth.clamp(scrollOffset, this.getHeight() - totalHeight, 0);
                return true;
            }
            return false;
        }

        private float collectElements(List<UIElement> toRender) {
            int height = 0;
            float startY = Integer.MAX_VALUE;
            for (var child : children) {
                float min = scrollOffset + height;
                float max = scrollOffset + child.getHeight() + height;
                height += child.getHeight();
                if (max < 0) {
                    continue;
                }
                if (min > this.getHeight()) {
                    break;
                }
                if (startY > min) {
                    startY = min;
                }
                toRender.add(child);
            }
            return startY;
        }

        public enum Alignment {
            LEFT,
            CENTER,
            RIGHT
        }
    }
}
