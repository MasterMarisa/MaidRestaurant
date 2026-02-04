package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

import java.awt.*;

public class UIButton extends UIElement {
    private final Button button;

    public UIButton(Rectangle frame, Button button) {
        super(frame);
        this.button = button;
    }

    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        button.setX(getCenterX() - button.getWidth() / 2);
        button.setY(getCenterY() - button.getHeight() / 2);
    }
}
