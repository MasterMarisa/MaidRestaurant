package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class UIBox extends UIElement {
    public Color color;

    public static UIBox horizontalLine(int minX, int maxX, int y, Color color) {
        return new UIBox(new Rectangle(minX, y, maxX + 1 - minX, 1), color);
    }

    public static UIBox verticalLine(int x, int minY, int maxY, Color color) {
        return new UIBox(new Rectangle(x, minY, 1, maxY + 1 - minY), color);
    }

    public UIBox(Rectangle frame, Color color) {
        super(frame);
        this.color = color;
    }

    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        graphics.fill(this.frame.x, this.frame.y, this.frame.x + this.frame.width, this.frame.y + this.frame.height, this.color.getRGB());
    }
}
