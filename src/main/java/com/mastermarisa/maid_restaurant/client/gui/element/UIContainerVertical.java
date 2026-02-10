package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIContainerVertical extends UIElement{
    public ElementAlignment alignment;
    public int padding;
    public int spacing;

    public UIContainerVertical(Rectangle frame, ElementAlignment alignment){
        super(frame);
        this.alignment = alignment;
    }

    public static UIContainerVertical wrap(List<? extends UIElement> elements, int spacing, int padding, ElementAlignment alignment){
        int x = 0;
        int y = padding * 2;
        for (UIElement element : elements){
            x = Math.max(x,element.getWidth());
            y += element.getHeight();
        }
        x += padding * 2;
        y += (elements.size() - 1) * spacing;

        UIContainerVertical container = new UIContainerVertical(new Rectangle(0,0,x,y),alignment);
        container.padding = padding;
        container.spacing = spacing;
        container.children = new ArrayList<>(elements.stream().map(UIElement::toUIElement).toList());

        return container;
    }

    protected void render(GuiGraphics graphics, int mouseX, int mouseY){
        order();

        super.render(graphics,mouseX,mouseY);
    }

    public void order() {
        int startY;

        switch (alignment){
            case UP:
                startY = frame.y + padding;

                for (UIElement child : children){
                    child.setCenterX(getCenterX());
                    child.frame.y = startY;
                    startY += child.getHeight() + spacing;
                }
                break;
            case CENTER:
                startY = getCenterY();

                for (UIElement child : children){
                    startY -= (child.getHeight() + spacing) / 2;
                }

                for (UIElement child : children){
                    child.setCenterX(getCenterX());
                    child.frame.y = startY;
                    startY += child.getHeight() + spacing;
                }
                break;
            case DOWN:
                startY = getMaxY() - padding;

                for (UIElement child : children){
                    startY -= child.getHeight();
                    child.setCenterX(getCenterX());
                    child.frame.y = startY;
                    startY -= spacing;
                }
                break;
        }
    }

    public static enum ElementAlignment{
        UP(0),
        CENTER(1),
        DOWN(2);

        final int ordinal;

        private ElementAlignment(int ordinal) { this.ordinal = ordinal; }
    }
}
