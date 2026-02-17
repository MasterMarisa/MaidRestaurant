package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.base.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.base.UIItemStackWithCount;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class UIOrderTag extends UIElement {
    private final UIImage bg;
    private final UIItemStackWithCount result;
    private final UICancelButton btn;
    protected OrderingScreen screen;
    protected int index;

    public UIOrderTag(RecipeData data, int count, OrderingScreen screen, int index) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.orderedTagImage);
        result = new UIItemStackWithCount(data.result.copyWithCount(data.result.getCount() * count),UIConst.lessBlack);
        result.dropShadow = false;
        this.screen = screen;
        this.index = index;
        btn = new UICancelButton(index,screen);

        children = List.of(bg,result,btn);
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics,mouseX,mouseY);
        resize();
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
        result.setMaxX(bg.getMaxX() - 21);
        result.setCenterY(getCenterY());
        btn.setMinX(getMinX() + 5);
        btn.setCenterY(getCenterY());
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (bg.frame.contains(mouseX,mouseY)) {
            if (scrollY > 0) {
                OrderingScreen.Order order = screen.orders.get(index);
                RecipeData data = order.data;
                if (data.result.getCount() * (order.count + 1) <= data.result.getMaxStackSize()) {
                    order.count++;
                    result.setCount(data.result.getCount() * order.count);
                }
                return true;
            } else if (scrollY < 0) {
                OrderingScreen.Order order = screen.orders.get(index);
                RecipeData data = order.data;
                if (order.count > 1) {
                    order.count--;
                    result.setCount(data.result.getCount() * order.count);
                }
                return true;
            }
        }

        return false;
    }
}
