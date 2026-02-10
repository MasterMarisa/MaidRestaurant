package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.element.UIButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class UIOrderButton extends UIButton {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/food_book.png");
    public static final int width = 16;
    public static final int height = 16;
    protected final OrderingScreen screen;
    protected final int index;

    public UIOrderButton(int index, OrderingScreen screen){
        super(new Rectangle(16,16),(button) -> {
            screen.order(index);
            return true;
        });
        this.screen = screen;
        this.index = index;
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics,mouseX,mouseY);
        if (screen.getCurPage() != null && screen.getCurPage().data.size() > index) {
            int x = frame.contains(mouseX,mouseY) ? 48 : 32;
            int y = 224;

            graphics.blit(texture, getMinX(), getMinY(), x, y, width, height,161,256);
        }
    }
}
