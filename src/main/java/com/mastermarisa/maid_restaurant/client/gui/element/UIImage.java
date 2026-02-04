package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class UIImage extends UIElement {
    public ImageData data;
    public float alpha;

    public UIImage(ImageData data) {
        this(new Rectangle(data.visualWidth, data.visualHeight), data);
    }

    public UIImage(Rectangle frame, ImageData data) {
        super(frame);
        this.alpha = 1.0F;
        this.data = data;
    }

    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        int imageWidth = this.data.partOfTexture.width;
        int imageHeight = this.data.partOfTexture.height;
        graphics.blit(this.data.textureLocation, this.frame.x + (int)Math.floor((double)(this.frame.width - imageWidth) / (double)2.0F), this.frame.y + (int)Math.floor((double)(this.frame.height - imageHeight) / (double)2.0F), 0, (float)this.data.partOfTexture.x, (float)this.data.partOfTexture.y, this.data.partOfTexture.width, this.data.partOfTexture.height, data.textureWidth, data.textureHeight);
    }
}
