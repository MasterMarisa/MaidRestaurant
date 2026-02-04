package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.init.UIConst;

import java.util.ArrayList;
import java.util.List;

public class ShowRecipePage extends UIPage {
    public final List<RecipeData> datas;
    private final UIContainerVertical page;

    public ShowRecipePage(List<RecipeData> datas) {
        super();
        this.datas = datas;

        List<UIElement> lines = new ArrayList<>();
        for (RecipeData data : datas) {
            UIItemStack icon = new UIItemStack(data.icon);
            UIItemStackWithCount result = new UIItemStackWithCount(data.result, UIConst.lessBlack);
            UIImage arrow = new UIImage(UIConst.arrowBrightImage);
            result.dropShadow = false;
            UIContainerHorizontal containerHorizontal = UIContainerHorizontal.wrap(List.of(icon,arrow,result),3,0, UIContainerHorizontal.ElementAlignment.LEFT);
            containerHorizontal.setWidth(108);
            lines.add(containerHorizontal);
            lines.add(UIBox.horizontalLine(-54,54,0,UIConst.leastBlack));
        }
        page = UIContainerVertical.wrap(lines,1,0, UIContainerVertical.ElementAlignment.UP);
        children = List.of(bg,page);
        onSwitchedTo();
    }

    @Override
    public void onResize() {
        super.onResize();
        page.setCenterX(getCenterX());
        page.setMinY(getMinY() + 10);
    }

    @Override
    public void onSwitchedTo() {
        onResize();
    }
}
