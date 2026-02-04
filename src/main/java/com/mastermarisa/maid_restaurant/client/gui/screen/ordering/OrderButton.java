package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OrderButton extends Button {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/food_book.png");
    public static final int width = 16;
    public static final int height = 16;
    private final OrderingScreen screen;
    private final int index;

    public OrderButton(int x,int y,int index,OrderingScreen screen){
        super(x, y, 16, 16, CommonComponents.EMPTY, (button -> screen.order(index)), DEFAULT_NARRATION);
        this.screen = screen;
        this.index = index;
    }

    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int x;
            int y = 224;
            if (isHovered()) {
                x = 48;
            } else {
                x = 32;
            }

            graphics.blit(texture, this.getX(), this.getY(), x,y,width,height,161,256);
        }
    }

    public void updateState() {
        this.visible = this.screen.getCurrentPage().datas.size() > index;
    }
}
