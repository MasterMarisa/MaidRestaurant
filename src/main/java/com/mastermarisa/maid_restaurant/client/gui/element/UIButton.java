package com.mastermarisa.maid_restaurant.client.gui.element;

import com.mastermarisa.maid_restaurant.api.functional.Func;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class UIButton extends UIElement {
    @Nullable
    protected Func<UIButton,Boolean> onClicked;

    public UIButton(Rectangle frame, @Nullable Func<UIButton,Boolean> onClicked) {
        super(frame);
        this.onClicked = onClicked;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (onClicked != null && frame.contains(mouseX,mouseY) && button == 0) {
            return onClicked.accept(this);
        }

        return super.onMouseClicked(mouseX, mouseY, button);
    }
}
