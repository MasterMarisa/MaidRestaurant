package com.mastermarisa.maid_restaurant.client.gui.base;

import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public final class ImageData {
    public final ResourceLocation textureLocation;
    public final Rectangle partOfTexture;
    public int visualWidth;
    public int visualHeight;
    public int textureWidth;
    public int textureHeight;

    public ImageData(ResourceLocation textureLocation, Rectangle partOfTexture, int visualWidth, int visualHeight, int textureWidth, int textureHeight) {
        this.textureLocation = textureLocation;
        this.partOfTexture = partOfTexture;
        this.visualWidth = visualWidth;
        this.visualHeight = visualHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }
}
