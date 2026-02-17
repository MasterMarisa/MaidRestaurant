package com.mastermarisa.maid_restaurant.client.gui.base;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

public class UIButton extends UIElement {
    @Nullable
    protected Consumer<UIButton> onClicked;
    protected int button;

    public UIButton(Rectangle frame, @Nullable Consumer<UIButton> onClicked, int button) {
        super(frame);
        this.onClicked = onClicked;
        this.button = button;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (onClicked != null && frame.contains(mouseX,mouseY) && (this.button == button || this.button == -1)) {
            onClicked.accept(this);
            return true;
        }

        return super.onMouseClicked(mouseX, mouseY, button);
    }
}
