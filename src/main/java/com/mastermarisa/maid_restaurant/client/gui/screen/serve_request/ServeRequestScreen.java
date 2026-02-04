package com.mastermarisa.maid_restaurant.client.gui.screen.serve_request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.client.gui.element.UIContainerHorizontal;
import com.mastermarisa.maid_restaurant.client.gui.element.UIContainerVertical;
import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequest;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mastermarisa.maid_restaurant.network.CancelRequestPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ServeRequestScreen extends Screen {
    private static final Minecraft mc;
    private static final Font font;
    private static final int startY = 50;

    protected final EntityMaid maid;
    protected ServeRequestQueue serveRequestQueue;

    protected final List<UIBasket> basketList;
    protected UIContainerVertical containerVertical;

    protected final List<CancelServeRequestButton> cancelButtons;

    public ServeRequestScreen(EntityMaid maid) {
        super(Component.empty());
        this.maid = maid;
        this.basketList = new ArrayList<>();
        this.cancelButtons = new ArrayList<>();
        initRequests();
        resize();
    }

    public static void open(EntityMaid maid) {
        mc.setScreen(new ServeRequestScreen(maid));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics,mouseX,mouseY,partialTicks);
        PoseStack poseStack = graphics.pose();
        containerVertical.order();
        for (var basket : basketList) {
            basket.resize();

            poseStack.pushPose();
            poseStack.translate(0,0,100);
            UIElement.render(graphics,basket.part1,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,200);
            graphics.renderItem(basket.item.itemStack,basket.item.getMinX(),basket.item.getMinY());
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,300);
            UIElement.render(graphics,basket.part2,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,400);
            UIElement.render(graphics,basket.part3,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,500);
            UIElement.render(graphics,basket.arrow,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,500);
            UIElement.render(graphics,basket.tables,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,500);
            UIElement.render(graphics,basket.bubble,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,500);
            UIElement.render(graphics,basket.count,mouseX,mouseY);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,700);
            for (var btn : cancelButtons)
                btn.render(graphics,mouseX,mouseY,partialTicks);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0,0,800);
            UIElement.renderToolTip(graphics,basket.item,mouseX,mouseY);
            poseStack.popPose();
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resize();
    }

    public void initRequests() {
        basketList.clear();
        if (checkAvailability()) {
            for (Iterator<ServeRequest> it = serveRequestQueue.iterator(); it.hasNext(); ) {
                var request = it.next();
                basketList.add(new UIBasket(request));
            }
            containerVertical = UIContainerVertical.wrap(basketList,3,0, UIContainerVertical.ElementAlignment.UP);
            containerVertical.order();
            for (var item : basketList)
                item.resize();
            resize();
        }
    }

    public void initButtons() {
        for (var btn : cancelButtons)
            this.removeWidget(btn);

        cancelButtons.clear();
        for (int i = 0;i < 4;i++) {
            cancelButtons.add(this.addRenderableWidget(new CancelServeRequestButton(getScreenCenterX() - 9,startY - 1 + i * 40,i,this)));
        }
        updateButtonVisibility();
    }

    public void resize() {
        containerVertical.setCenterX(getScreenCenterX());
        containerVertical.setMinY(startY);
        initButtons();
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
            serveRequestQueue = maid.getData(ServeRequestQueue.TYPE);
            return true;
        }
        close();
        return false;
    }

    public void updateButtonVisibility() {
        for (var btn : cancelButtons)
            btn.updateState();
    }

    public void cancelRequest(int index) {
        if (checkAvailability()) {
            if (index < basketList.size()) {
                serveRequestQueue.removeAt(index);
                CancelRequestPayload payload = new CancelRequestPayload(1,maid.getUUID().toString(),index);
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
