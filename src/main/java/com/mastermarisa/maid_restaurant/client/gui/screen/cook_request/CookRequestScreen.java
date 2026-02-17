package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.UIContainerHorizontal;
import com.mastermarisa.maid_restaurant.client.gui.base.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.base.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements.UIChangeAcceptValueButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements.UICookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class CookRequestScreen extends Screen {
    private static final Minecraft mc;
    private static final Font font;

    public final EntityMaid maid;
    public CookRequestHandler handler;

    private final UIImage band;
    private UIContainerHorizontal container;
    private final UIChangeAcceptValueButton acceptValueButton;

    public CookRequestScreen(EntityMaid maid) {
        super(Component.empty());
        this.maid = maid;
        initRequests();
        band = new UIImage(UIConst.bandImage);
        band.setCenter(getScreenCenterX(),getScreenCenterY() - 35);
        acceptValueButton = new UIChangeAcceptValueButton(CookRequest.TYPE,maid.getUUID(),maid.getData(CookRequestHandler.TYPE));
        acceptValueButton.setCenter(band.getMinX() - 10,band.getCenterY() + 43);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        UIElement.render(graphics,band,mouseX,mouseY);
        UIElement.render(graphics,container,mouseX,mouseY);
        UIElement.render(graphics,acceptValueButton,mouseX,mouseY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        band.setCenter(getScreenCenterX(),getScreenCenterY() - 35);
        initRequests();
        acceptValueButton.setCenter(band.getMinX() - 10,band.getCenterY() + 43);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        UIElement.onMouseClicked(container,mouseX,mouseY,button);
        UIElement.onMouseClicked(acceptValueButton,mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void initRequests() {
        if (!checkAvailability()) return;
        List<UICookRequest> requests = new ArrayList<>();
        for (int i = 0;i < handler.size();i++) {
            requests.add(new UICookRequest(i,this));
        }

        container = UIContainerHorizontal.wrap(requests,3,0, UIContainerHorizontal.ElementAlignment.LEFT);
        container.setWidth(144);
        container.setCenter(getScreenCenterX(),getScreenCenterY() - 11);
        container.order();
    }

    public boolean checkAvailability() {
        if (maid == null) {
            close();
            return false;
        }
        handler = maid.getData(CookRequestHandler.TYPE);
        return true;
    }

    public static void open(EntityMaid maid) { mc.setScreen(new CookRequestScreen(maid)); }

    public void close() {
        mc.setScreen(null);
    }

    public static int getScreenCenterX(){
        return mc.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return mc.getWindow().getGuiScaledHeight() / 2;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }
}
