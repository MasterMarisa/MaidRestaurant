package com.mastermarisa.maid_restaurant.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record ImageData(ResourceLocation location, int u, int v, int width, int height, int textureWidth, int textureHeight) {
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void renderCentered(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x - width / 2, y - height / 2, u, v, width, height, textureWidth, textureHeight);
    }
}
