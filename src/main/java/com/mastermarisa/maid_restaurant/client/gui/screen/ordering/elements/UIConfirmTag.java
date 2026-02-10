package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.element.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.init.UIConst;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIConfirmTag extends UIElement {
    private final UIImage bg;
    private final UIConfirmButton btn;

    public UIConfirmTag(OrderingScreen screen) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.confirmTagImage);
        btn = new UIConfirmButton(screen);

        children = new ArrayList<>(List.of(bg,btn));
        resize();
    }

    public void resize() {
        bg.setMaxX(getMaxX());
        bg.setCenterY(getCenterY());
        btn.setMinX(bg.getMinX() + 4);
        btn.setCenterY(getCenterY());
    }
}
