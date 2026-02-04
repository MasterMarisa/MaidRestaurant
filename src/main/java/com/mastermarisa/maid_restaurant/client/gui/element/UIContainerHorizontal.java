package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIContainerHorizontal extends UIElement{
    public ElementAlignment alignment;
    public int padding;
    public int spacing;

    public static UIContainerHorizontal wrap(List<? extends UIElement> elements, int spacing, int padding, ElementAlignment alignment){
        int x = padding * 2;
        int y = 0;
        for (UIElement element : elements){
            x += element.getWidth();
            y = Math.max(y,element.getHeight());
        }
        x += (elements.size() - 1) * spacing;
        y += padding * 2;

        UIContainerHorizontal container = new UIContainerHorizontal(new Rectangle(0,0,x,y),alignment);
        container.padding = padding;
        container.spacing = spacing;
        container.children = new ArrayList<>(elements.stream().map(UIElement::toUIElement).toList());

        return container;
    }

    public static UIContainerHorizontal wrap(Rectangle frame,List<? extends UIElement> elements,int spacing,int padding,ElementAlignment alignment){
        UIContainerHorizontal container = new UIContainerHorizontal(frame,alignment);
        container.padding = padding;
        container.spacing = spacing;
        container.children = elements.stream().map(UIElement::toUIElement).toList();

        return container;
    }

    public UIContainerHorizontal(Rectangle frame,ElementAlignment alignment){
        super(frame);
        this.alignment = alignment;
    }

    protected void render(GuiGraphics graphics) {
        order();

        super.render(graphics);
    }

    public void order() {
        int startX;

        switch (alignment){
            case LEFT:
                startX = frame.x + padding;

                for (UIElement child : children){
                    child.frame.x = startX;
                    child.setCenterY(getCenterY());
                    startX += child.getWidth() + spacing;
                }
                break;
            case CENTER:
                startX = getCenterX();
                for (UIElement child : children){
                    startX -= (child.getWidth() + spacing) / 2;
                }

                for (UIElement child : children){
                    child.frame.x = startX;
                    child.setCenterY(getCenterY());
                    startX += child.getWidth() + spacing;
                }
                break;
            case RIGHT:
                startX = getMaxX() - padding;

                for (UIElement child : children){
                    startX -= child.getWidth();
                    child.frame.x = startX;
                    child.setCenterY(getCenterY());
                    startX -= spacing;
                }
                break;
        }
    }

    public static enum ElementAlignment{
        LEFT(0),
        CENTER(1),
        RIGHT(2);

        final int ordinal;

        private ElementAlignment(int ordinal) { this.ordinal = ordinal; }
    }
}
