package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements;

import com.mastermarisa.maid_restaurant.api.request.RequestHandler;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.*;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.network.ChangeHandlerAcceptValuePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class UIChangeAcceptValueButton extends UIButton {
    private final int type;
    private final UUID uuid;
    private final RequestHandler<?> handler;
    private final UIItemStack order = new UIItemStack(ModItems.ORDER_ITEM.toStack());
    private final UIItemStack barrier = new UIItemStack(new ItemStack(Items.BARRIER));
    private final UIBox underLine = UIBox.horizontalLine(-7,7,0,new Color(1,1,1,0.2f));

    public UIChangeAcceptValueButton(int type, UUID uuid, RequestHandler<?> handler) {
        super(new Rectangle(16,16), (button) -> ((UIChangeAcceptValueButton)button).trigger(), 0);
        this.type = type;
        this.uuid = uuid;
        this.handler = handler;
        order.tooltip.add(Component.translatable("gui.maid_restaurant.request_handler.accept"));
        barrier.tooltip.add(Component.translatable("gui.maid_restaurant.request_handler.no_accept"));
        children = List.of(underLine);
    }

    @Override
    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        underLine.setCenter(getCenterX() - 1,getMaxY() + 2);
        super.render(graphics, mouseX, mouseY);
        if (handler.accept) {
            order.setCenter(getCenterX(),getCenterY());
            UIElement.render(graphics,order,mouseX,mouseY);
        } else {
            barrier.setCenter(getCenterX(),getCenterY());
            UIElement.render(graphics,barrier,mouseX,mouseY);
        }
    }

    public void trigger() {
        handler.accept = !handler.accept;

        ChangeHandlerAcceptValuePayload payload = new ChangeHandlerAcceptValuePayload(type,uuid,handler.accept);
        PacketDistributor.sendToServer(payload);
    }
}
