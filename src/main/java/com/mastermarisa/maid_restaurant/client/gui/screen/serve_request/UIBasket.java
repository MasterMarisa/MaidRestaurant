package com.mastermarisa.maid_restaurant.client.gui.screen.serve_request;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequest;
import com.mastermarisa.maid_restaurant.init.UIConst;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIBasket extends UIElement {
    public final UIImage part1;
    public final UIImage part2;
    public final UIImage part3;
    public final UIItemStack item;
    public final UIImage arrow;
    public final UIItemStackWithCount tables;
    public final UIImage bubble;
    public final UILabel count;

    public UIBasket(ServeRequest request) {
        super(new Rectangle(106,37));
        part1 = new UIImage(UIConst.basket_1);
        part2 = new UIImage(UIConst.basket_2);
        part3 = new UIImage(UIConst.basket_3);
        item = new UIItemStack(request.toServe);
        arrow = new UIImage(UIConst.arrowBrightImage);
        tables = new UIItemStackWithCount(new ItemStack(ModItems.TABLE_OAK.get(),request.targetTables.size()));
        bubble = new UIImage(UIConst.bubble);
        count = new UILabel((request.requestedCount - request.toServe.getCount()) + "/" + request.requestedCount,Color.WHITE);
        count.dropShadow = true;
        resize();
        children = new ArrayList<>(List.of(part1,item,part2,part3));
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics,mouseX,mouseY);
        resize();
    }

    public void resize() {
        part1.setMinX(getMinX());
        part1.setMinY(getMinY());
        item.setCenterX(part1.getCenterX());
        item.setMinY(getMinY() + 9);
        part2.setMinX(getMinX());
        part2.setMinY(getMinY());
        part3.setMinX(getMinX());
        part3.setMaxY(getMaxY() - 4);
        arrow.setMinX(part1.getMaxX() + 11);
        arrow.setCenterY(getCenterY());
        tables.setMinX(arrow.getMaxX() + 11);
        tables.setCenterY(getCenterY());
        count.setCenterY(getCenterY() + 10);
        count.setCenterX(arrow.getCenterX());
        bubble.setCenterX(arrow.getCenterX());
        bubble.setCenterY(getCenterY() - 14);
    }
}
