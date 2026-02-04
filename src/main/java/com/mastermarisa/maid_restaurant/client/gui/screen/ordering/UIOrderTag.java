package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.element.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.element.UIItemStackWithCount;
import com.mastermarisa.maid_restaurant.init.UIConst;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.List;

public class UIOrderTag extends UIElement {
    private final UIImage bg;
    private final UIItemStackWithCount result;

    public UIOrderTag(RecipeData data, int count) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.orderedTagImage);
        result = new UIItemStackWithCount(data.result.copyWithCount(data.result.getCount() * count),UIConst.lessBlack);
        result.dropShadow = false;
        children = List.of(bg,result);
    }

    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        resize();
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
        result.setMaxX(bg.getMaxX() - 21);
        result.setCenterY(getCenterY());
    }
}
