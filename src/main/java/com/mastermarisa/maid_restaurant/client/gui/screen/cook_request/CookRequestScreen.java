package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.element.UIContainerHorizontal;
import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.element.UIImage;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mastermarisa.maid_restaurant.init.UIConst;
import com.mastermarisa.maid_restaurant.network.CancelRequestPayload;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import com.mastermarisa.maid_restaurant.uitls.manager.RequestManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CookRequestScreen extends Screen {
    private static final Minecraft mc;
    private static final Font font;
    private static final int startY = 80;

    protected final EntityMaid maid;
    protected CookRequestQueue cookRequestQueue;
    protected ServeRequestQueue serveRequestQueue;

    protected final UIImage band;
    protected UIContainerHorizontal requests;
    protected List<UICookRequest> tags;
    protected List<CancelCookRequestButton> cancelButtons;

    public CookRequestScreen(EntityMaid maid) {
        super(Component.empty());
        this.maid = maid;
        this.band = new UIImage(UIConst.bandImage);
        this.requests = new UIContainerHorizontal(new Rectangle(0,0), UIContainerHorizontal.ElementAlignment.LEFT);
        this.tags = new ArrayList<>();
        this.cancelButtons = new ArrayList<>();
        initRequests();
        resize();
    }

    public static void open(EntityMaid maid) {
        mc.setScreen(new CookRequestScreen(maid));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics,mouseX,mouseY,partialTicks);
        UIElement.render(graphics,band,mouseX,mouseY);
        UIElement.render(graphics,requests,mouseX,mouseY);
        for (var btn : cancelButtons)
            btn.render(graphics,mouseX,mouseY,partialTicks);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resize();
    }

    public void resize() {
        band.setCenterX(getScreenCenterX());
        band.setMinY(startY);
        requests.setCenterX(getScreenCenterX());
        requests.setMinY(startY - 2);
        requests.order();
        for (var tag : tags)
            tag.resize();
        initButtons();
    }

    public void initRequests() {
        if (checkAvailability()) {
            tags = new ArrayList<>();
            for (Iterator<CookRequest> it = cookRequestQueue.iterator(); it.hasNext(); ) {
                CookRequest request = it.next();
                if (checkAvailability()) {
                    tags.add(new UICookRequest(request, CookTaskManager.getTask(request.type).get()));
                }
            }
            requests = UIContainerHorizontal.wrap(tags,3,0, UIContainerHorizontal.ElementAlignment.LEFT);
            requests.setWidth(142);
            requests.order();
            for (var tag : tags)
                tag.resize();
        }
    }

    public void initButtons() {
        for (var btn : cancelButtons)
            this.removeWidget(btn);

        cancelButtons = new ArrayList<>();

        for (int i = 0;i < 7;i++) {
            cancelButtons.add(this.addRenderableWidget(new CancelCookRequestButton(requests.getMinX() + 5 + 21 * i,requests.getMaxY() - 13,i,this)));
        }
        updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        for (var btn : cancelButtons)
            btn.updateState();
    }

    public void close() {
        mc.setScreen(null);
    }

    public static int getScreenCenterX(){
        return mc.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return mc.getWindow().getGuiScaledHeight() / 2;
    }

    public boolean checkAvailability() {
        if (maid != null) {
            cookRequestQueue = maid.getData(CookRequestQueue.TYPE);
            serveRequestQueue = maid.getData(ServeRequestQueue.TYPE);
            if (cookRequestQueue.size() != 0) {
                return true;
            }
        }
        close();
        return false;
    }

    public void cancelRequest(int index) {
        if (checkAvailability()) {
            if (index < tags.size()) {
                cookRequestQueue.removeAt(index);
                serveRequestQueue.removeAt(index);
                CancelRequestPayload payload = new CancelRequestPayload(0,maid.getUUID().toString(),index);
                PacketDistributor.sendToServer(payload);
                initRequests();
                resize();
            }
        }
    }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }
}
