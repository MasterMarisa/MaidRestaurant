package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CancelCookRequestButton extends Button {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/cross.png");
    private final CookRequestScreen screen;
    private final int index;

    public CancelCookRequestButton(int x,int y,int index,CookRequestScreen screen){
        super(x, y, 7, 7, CommonComponents.EMPTY, (button -> screen.cancelRequest(index)), DEFAULT_NARRATION);
        this.screen = screen;
        this.index = index;
    }

    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            graphics.blit(texture, this.getX(), this.getY(), 0,0,7,7,7,7);
        }
    }

    public void updateState() {
        this.visible = this.screen.tags.size() > index;
    }
}
