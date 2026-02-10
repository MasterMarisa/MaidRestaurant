package com.mastermarisa.maid_restaurant.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class UILabel extends UIElement {
    public String text;
    public TextAlignment alignment;
    public Color color;
    public boolean dropShadow;

    public UILabel(String text) {
        this(new Rectangle(font.width(text) - 1, 7), text);
    }

    public UILabel(String text, Color color){
        this(new Rectangle(font.width(text) - 1,7),text,color);
    }

    public UILabel(Rectangle frame, String text) {
        super(frame);
        this.text = "";
        this.alignment = UILabel.TextAlignment.CENTER;
        this.color = Color.BLACK;
        this.text = text;
    }

    public UILabel(Rectangle frame, String text, Color color) {
        super(frame);
        this.text = "";
        this.alignment = UILabel.TextAlignment.CENTER;
        this.color = color;
        this.text = text;
    }

    public UILabel(Rectangle frame) {
        super(frame);
        this.text = "";
        this.alignment = UILabel.TextAlignment.CENTER;
        this.color = Color.BLACK;
    }

    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics,mouseX,mouseY);
        int textWidth = font.width(this.text) - 1;
        int x = this.frame.x + (this.frame.width - textWidth) * this.alignment.ordinal / 2;
        int y = this.frame.y + (this.frame.height - 7) / 2;
        if (this.color.getTransparency() == 3) {
            RenderSystem.enableBlend();
        }

        graphics.drawString(font, this.text, x, y, this.color.getRGB(), dropShadow);
    }

    public static enum TextAlignment {
        LEFT(0),
        CENTER(1),
        RIGHT(2);

        final int ordinal;

        private TextAlignment(int ordinal) {
            this.ordinal = ordinal;
        }
    }
}
