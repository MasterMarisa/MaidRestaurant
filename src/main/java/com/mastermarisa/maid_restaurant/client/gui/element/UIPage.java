package com.mastermarisa.maid_restaurant.client.gui.element;

import com.mastermarisa.maid_restaurant.init.UIConst;

import java.awt.*;
import java.util.List;

public class UIPage extends UIElement {
    public final UIImage bg;

    public UIPage(Rectangle frame){
        super(frame);
        bg = new UIImage(UIConst.bookImage);

        this.children = java.util.List.of(bg);
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
    }

    public UIPage(){
        super(new Rectangle(148,180));
        bg = new UIImage(UIConst.bookImage);

        this.children = List.of(bg);
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
    }

    public void onResize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
    }

    public void onSwitchedTo() {
        onResize();
    }
}
