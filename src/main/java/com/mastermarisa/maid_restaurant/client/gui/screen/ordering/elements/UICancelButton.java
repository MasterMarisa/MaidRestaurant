package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.element.UIButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class UICancelButton extends UIButton {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/cross.png");
    private final OrderingScreen screen;
    private final int index;

    public UICancelButton(int index, OrderingScreen screen) {
        super(new Rectangle(7,7),(button) -> {
            screen.cancel(index);
            return true;
        });
        this.screen = screen;
        this.index = index;
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics,mouseX,mouseY);
        if (screen.orders.size() > index) {
            graphics.blit(texture, getMinX(), getMinY(), 0,0,7,7,7,7);
        }
    }
}
