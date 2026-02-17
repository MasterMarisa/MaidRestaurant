package com.mastermarisa.maid_restaurant.client.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;


public class UIEditBox extends UIElement {
    private final EditBox editBox;
    private final UIBox bg;

    public UIEditBox(Rectangle frame, Color color, int maxLength, Consumer<String> responder, Color textColor) {
        super(frame);

        this.editBox = new EditBox(font,
                (int) frame.getMinX(),
                (int) frame.getMinY(),
                frame.width,
                frame.height,
                Component.literal("")
        );

        this.editBox.setMaxLength(maxLength);
        this.editBox.setBordered(false);
        this.editBox.setVisible(true);
        this.editBox.setCanLoseFocus(true);
        this.editBox.setFocused(false);
        this.editBox.setResponder(responder);
        this.editBox.setTextColor(textColor.getRGB());

        this.bg = new UIBox(new Rectangle(frame.width + 6,frame.height),color);
        this.children = List.of(bg);
    }

    @Override
    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        resize();
        super.render(graphics, mouseX, mouseY);
        //editBox.render(graphics,mouseX,mouseY,0);
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY() - 3);
        editBox.setX(getMinX());
        editBox.setY(getMinY());
    }

    public EditBox getEditBox() { return editBox; }
}
