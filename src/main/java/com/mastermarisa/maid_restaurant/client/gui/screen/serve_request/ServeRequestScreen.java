package com.mastermarisa.maid_restaurant.client.gui.screen.serve_request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.client.gui.base.UIContainerVertical;
import com.mastermarisa.maid_restaurant.client.gui.base.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements.UIChangeAcceptValueButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements.UICookRequest;
import com.mastermarisa.maid_restaurant.client.gui.screen.serve_request.elements.UIServeRequest;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ServeRequestScreen extends Screen {
    private static final Minecraft mc;
    private static final Font font;

    public final EntityMaid maid;
    public ServeRequestHandler handler;

    private UIContainerVertical container;

    public ServeRequestScreen(EntityMaid maid) {
        super(Component.empty());
        this.maid = maid;
        initRequests();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        UIElement.render(graphics,container,mouseX,mouseY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        initRequests();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        UIElement.onMouseClicked(container,mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean checkAvailability() {
        if (maid == null) {
            close();
            return false;
        }
        handler = maid.getData(ServeRequestHandler.TYPE);
        return true;
    }

    public void initRequests() {
        if (!checkAvailability()) return;
        List<UIServeRequest> requests = new ArrayList<>();
        for (int i = 0;i < handler.size();i++) {
            requests.add(new UIServeRequest(i,this));
        }

        container = UIContainerVertical.wrap(requests,3,0, UIContainerVertical.ElementAlignment.CENTER);
        container.setCenter(getScreenCenterX(),getScreenCenterY());
        container.order();
    }

    public static void open(EntityMaid maid) { mc.setScreen(new ServeRequestScreen(maid)); }

    public void close() {
        mc.setScreen(null);
    }

    public static int getScreenCenterX(){
        return mc.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return mc.getWindow().getGuiScaledHeight() / 2;
    }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }
}
