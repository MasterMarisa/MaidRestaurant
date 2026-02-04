package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request;

import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.element.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.element.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.element.UIItemStack;
import com.mastermarisa.maid_restaurant.client.gui.element.UILabel;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.init.UIConst;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UICookRequest extends UIElement {
    protected final UIImage bg;
    protected final UIItemStack result;
    protected final UILabel count;

    public UICookRequest(CookRequest request, ICookTask iCookTask) {
        super(new Rectangle(18,70));
        bg = new UIImage(UIConst.requestImage);
        RecipeHolder<? extends Recipe<?>> holder = RecipeUtils.byKeyTyped(request.type,request.id);
        result = new UIItemStack(iCookTask.getResult(holder));
        count = new UILabel(String.valueOf(iCookTask.getResult(holder).getCount() * request.requestedCount),UIConst.lessBlack);
        children = new ArrayList<>(List.of(bg,result,count));
    }

    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        resize();
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
        result.setCenterX(getCenterX());
        result.setMinY(getMinY() + 24);
        count.setCenterX(getCenterX());
        count.setMinY(getMinY() + 42);
    }
}
