package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements;

import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.UIButton;
import com.mastermarisa.maid_restaurant.client.gui.base.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.network.CancelRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;

@ParametersAreNonnullByDefault
public class UICancelRequestButton extends UIButton {
    private final int index;
    private final CookRequestScreen screen;
    private final UIImage img;

    public UICancelRequestButton(int index, CookRequestScreen screen) {
        super(new Rectangle(8,8), (button) -> ((UICancelRequestButton)button).trigger(), 0);
        this.index = index;
        this.screen = screen;
        img = new UIImage(UIConst.cross);

        children = List.of(img);
    }

    @Override
    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        img.setCenter(getCenterX(),getCenterY());
        super.render(graphics, mouseX, mouseY);
    }

    public void trigger() {
        if (!screen.checkAvailability()) return;

        screen.handler.removeAt(index);
        CancelRequestPayload payload = new CancelRequestPayload(0,screen.maid.getUUID(),index);
        PacketDistributor.sendToServer(payload);

        screen.initRequests();
    }
}
