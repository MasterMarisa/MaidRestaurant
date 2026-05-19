package com.mastermarisa.maid_restaurant.client.gui.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class UIElement {
    protected static final Minecraft minecraft;
    protected static final Font font;

    public Rectangle frame;
    public List<UIElement> children = new ArrayList<>();

    public UIElement(Rectangle frame) {
        this.frame = frame;
    }

    public final int getCenterX() {
        return this.frame.x + this.frame.width / 2;
    }

    public final void setCenterX(int centerX) {
        this.frame.setLocation(centerX - this.frame.width / 2, this.frame.y);
    }

    public final int getCenterY() {
        return this.frame.y + this.frame.height / 2;
    }

    public final void setCenterY(int centerY) {
        this.frame.setLocation(this.frame.x, centerY - this.frame.height / 2);
    }

    public final int getMinX() {
        return this.frame.x;
    }

    public final void setMinX(int minX) {
        this.frame.setLocation(minX, this.frame.y);
    }

    public final int getMinY() {
        return this.frame.y;
    }

    public final void setMinY(int minY) {
        this.frame.setLocation(this.frame.x, minY);
    }

    public final int getMaxX() {
        return this.frame.x + this.frame.width;
    }

    public final void setMaxX(int maxX) {
        this.frame.setLocation(maxX - this.frame.width, this.frame.y);
    }

    public final int getMaxY() {
        return this.frame.y + this.frame.height;
    }

    public final void setMaxY(int maxY) {
        this.frame.setLocation(this.frame.x, maxY - this.frame.height);
    }

    public final int getWidth() {
        return this.frame.width;
    }

    public final void setWidth(int width) {
        this.frame.width = width;
    }

    public final int getHeight() {
        return this.frame.height;
    }

    public final void setHeight(int height) {
        this.frame.height = height;
    }

    static {
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }
}
