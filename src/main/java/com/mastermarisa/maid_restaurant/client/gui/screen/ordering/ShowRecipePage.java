package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UIOrderButton;
import com.mastermarisa.maid_restaurant.init.UIConst;

import java.util.ArrayList;
import java.util.List;

public class ShowRecipePage extends UIPage {
    public final List<RecipeData> data;
    private final UIContainerHorizontal page;

    public ShowRecipePage(List<RecipeData> data, OrderingScreen screen) {
        super();
        this.data = data;

        List<UIElement> lines = new ArrayList<>();
        for (int i = 0;i < Math.min(data.size(),8);i++) {
            RecipeData recipeData = data.get(i);
            UIItemStackWithCount result = new UIItemStackWithCount(recipeData.result, UIConst.lessBlack);
            result.dropShadow = false;
            UIOrderButton btn = new UIOrderButton(i,screen);
            UIContainerHorizontal container = UIContainerHorizontal.wrap(List.of(result,btn),13,0, UIContainerHorizontal.ElementAlignment.LEFT);
            container.setWidth(50);
            lines.add(container);
            lines.add(UIBox.horizontalLine(-25,25,0,UIConst.leastBlack));
        }
        UIContainerVertical col1 = UIContainerVertical.wrap(lines,1,0, UIContainerVertical.ElementAlignment.UP);
        col1.setWidth(50);
        col1.setHeight(160);

        lines = new ArrayList<>();
        if (data.size() > 8) {
            for (int i = 8;i < Math.min(data.size(),16);i++) {
                RecipeData recipeData = data.get(i);
                UIItemStackWithCount result = new UIItemStackWithCount(recipeData.result, UIConst.lessBlack);
                result.dropShadow = false;
                UIOrderButton btn = new UIOrderButton(i,screen);
                UIContainerHorizontal container = UIContainerHorizontal.wrap(List.of(result,btn),13,0, UIContainerHorizontal.ElementAlignment.LEFT);
                container.setWidth(50);
                lines.add(container);
                lines.add(UIBox.horizontalLine(-25,25,0,UIConst.leastBlack));
            }
        }
        UIContainerVertical col2 = UIContainerVertical.wrap(lines,1,0, UIContainerVertical.ElementAlignment.UP);
        col2.setWidth(50);
        col2.setHeight(160);
        page = UIContainerHorizontal.wrap(List.of(col1,col2),7,0, UIContainerHorizontal.ElementAlignment.LEFT);

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
