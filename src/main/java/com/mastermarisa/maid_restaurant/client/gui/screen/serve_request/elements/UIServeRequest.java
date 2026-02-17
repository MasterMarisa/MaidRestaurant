package com.mastermarisa.maid_restaurant.client.gui.screen.serve_request.elements;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.*;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements.UICancelRequestButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.serve_request.ServeRequestScreen;
import com.mastermarisa.maid_restaurant.network.CancelRequestPayload;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class UIServeRequest extends UIElement {
    public final UIImage part1;
    public final UIImage part2;
    public final UIImage part3;
    public final UIItemStack item;
    public final UIImage arrow;
    public final UIItemStackWithCount tables;
    public final UIImage bubble;
    public final UILabel count;
    public final UICancelRequestButton btn;

    public UIServeRequest(int index, ServeRequestScreen screen) {
        super(new Rectangle(106, 37));
        ServeRequest request = Objects.requireNonNull(screen.handler.getAt(index));
        this.part1 = new UIImage(UIConst.basket_1);
        this.part2 = new UIImage(UIConst.basket_2);
        this.part3 = new UIImage(UIConst.basket_3);
        this.item = new UIItemStack(request.toServe);
        this.arrow = new UIImage(UIConst.arrowBrightImage);
        this.tables = new UIItemStackWithCount(new ItemStack(ModItems.TABLE_OAK.get(), request.targets.size()));
        this.bubble = new UIImage(UIConst.bubble);
        this.count = new UILabel(request.requested - request.toServe.getCount() + "/" + request.requested, UILabel.TextAlignment.CENTER, Color.WHITE, true);
        btn = new UICancelRequestButton(index,screen);
        this.resize();
        this.children = new ArrayList<>(List.of(btn));
    }

    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics, mouseX, mouseY);
        this.resize();
        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 100.0F);
        UIElement.render(graphics, part1, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 200.0F);
        graphics.renderItem(item.itemStack, item.getMinX(), item.getMinY());
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 300.0F);
        UIElement.render(graphics, part2, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 400.0F);
        UIElement.render(graphics, part3, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 500.0F);
        UIElement.render(graphics, arrow, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 500.0F);
        UIElement.render(graphics, tables, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 500.0F);
        UIElement.render(graphics, bubble, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 500.0F);
        UIElement.render(graphics, count, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 700.0F);
        UIElement.render(graphics, btn, mouseX, mouseY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 800.0F);
        UIElement.renderToolTip(graphics, item, mouseX, mouseY);
        poseStack.popPose();
    }

    public void resize() {
        this.part1.setMinX(this.getMinX());
        this.part1.setMinY(this.getMinY());
        this.item.setCenterX(this.part1.getCenterX());
        this.item.setMinY(this.getMinY() + 9);
        this.part2.setMinX(this.getMinX());
        this.part2.setMinY(this.getMinY());
        this.part3.setMinX(this.getMinX());
        this.part3.setMaxY(this.getMaxY() - 4);
        this.arrow.setMinX(this.part1.getMaxX() + 11);
        this.arrow.setCenterY(this.getCenterY());
        this.tables.setMinX(this.arrow.getMaxX() + 11);
        this.tables.setCenterY(this.getCenterY());
        this.count.setCenterY(this.getCenterY() + 10);
        this.count.setCenterX(this.arrow.getCenterX());
        this.bubble.setCenterX(this.arrow.getCenterX());
        this.bubble.setCenterY(this.getCenterY() - 14);
        this.btn.setCenter(bubble.getCenterX(), bubble.getCenterY() - 2);
    }

    @ParametersAreNonnullByDefault
    public static class UICancelRequestButton extends UIButton {
        private final int index;
        private final ServeRequestScreen screen;
        private final UIImage img;

        public UICancelRequestButton(int index, ServeRequestScreen screen) {
            super(new Rectangle(7,7), (button) -> ((UICancelRequestButton)button).trigger(), 0);
            this.index = index;
            this.screen = screen;
            img = new UIImage(UIConst.cross_1);

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
            CancelRequestPayload payload = new CancelRequestPayload(1,screen.maid.getUUID(),index);
            PacketDistributor.sendToServer(payload);

            screen.initRequests();
        }
    }
}
