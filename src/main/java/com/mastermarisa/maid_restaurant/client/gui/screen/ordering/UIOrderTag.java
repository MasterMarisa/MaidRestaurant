package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.element.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.element.UIItemStackWithCount;
import com.mastermarisa.maid_restaurant.init.UIConst;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.tools.obfuscation.ObfuscationData;

import java.awt.*;
import java.util.List;

public class UIOrderTag extends UIElement {
    private final UIImage bg;
    private final UIItemStackWithCount result;
    protected OrderingScreen screen;
    protected int index;

    public UIOrderTag(RecipeData data, int count, OrderingScreen screen, int index) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.orderedTagImage);
        result = new UIItemStackWithCount(data.result.copyWithCount(data.result.getCount() * count),UIConst.lessBlack);
        result.dropShadow = false;
        this.screen = screen;
        this.index = index;
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

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (result.frame.contains(mouseX,mouseY)) {
            if (scrollY > 0) {
                screen.order(index);
                return true;
            } else if (scrollY < 0) {
                var pair = screen.orders.get(index);
                if (pair.right() > 1) {
                    screen.orders.set(index, Pair.of(pair.left(),pair.right()-1));
                }
                return true;
            }
        }

        return false;
    }
}
