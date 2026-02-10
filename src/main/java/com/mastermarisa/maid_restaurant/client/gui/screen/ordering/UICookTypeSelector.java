package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.gui.IPageable;
import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.init.UIConst;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class UICookTypeSelector extends UIElement implements IPageable {
    public static final int countPerPage = 5;

    private final UIImage bg;
    private final UIImage bubble;
    private final List<UIItemStack> icons;
    private final List<UIContainerHorizontal> pages;
    private final List<RecipeType<?>> types;
    private final OrderingScreen screen;
    private RecipeType<?> selectedType;
    private int curPageNum;

    public UICookTypeSelector(OrderingScreen screen) {
        super(new Rectangle(139,17));
        this.screen = screen;
        bg = new UIImage(UIConst.band_1);
        bubble = new UIImage(UIConst.typeBubble);
        selectedType = screen.curType;
        icons = new ArrayList<>();
        types = new ArrayList<>();
        pages = new ArrayList<>();
        for (RecipeType<?> type : CookTaskManager.getAllRegisteredTypes()) {
            ICookTask iCookTask = CookTaskManager.getTask(type).get();
            UIItemStack stack = new UIItemStack(iCookTask.getIcon());
            icons.add(stack);
            types.add(type);
        }

        for (int i = 0;i < types.size();i += countPerPage) {
            List<UIItemStack> elements = new ArrayList<>();
            for (int j = i;j < i + countPerPage;j++) {
                if (j < types.size()) {
                    elements.add(icons.get(j));
                }
            }
            UIContainerHorizontal container = UIContainerHorizontal.wrap(elements,10,0, UIContainerHorizontal.ElementAlignment.LEFT);
            container.setWidth(120);
            pages.add(container);
        }

        children = new ArrayList<>();
    }

    @Override
    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        resize();
        children.clear();
        children.add(bg);
        int pos = types.indexOf(selectedType);
        if (curPageNum * countPerPage <= pos && pos < (curPageNum + 1) * countPerPage) children.add(bubble);
        children.add(pages.get(curPageNum));
        super.render(graphics, mouseX, mouseY);
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
        pages.get(curPageNum).setCenterX(getCenterX());
        pages.get(curPageNum).setCenterY(getCenterY());
        pages.get(curPageNum).order();
        bubble.setMaxY(bg.getMaxY());
        bubble.setCenterX(icons.get(types.indexOf(selectedType)).getCenterX());
    }

    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        for (int i = curPageNum * countPerPage;i < Math.min(types.size(),(curPageNum + 1) * countPerPage);i++) {
            if (button == 0 && icons.get(i).frame.contains(mouseX,mouseY)) {
                screen.curType = types.get(i);
                screen.switchToPage(0);
                selectedType = types.get(i);
                resize();
                break;
            }
        }
        return super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (frame.contains(mouseX,mouseY)) {
            if (scrollY > 0) {
                switchToPage(curPageNum - 1);
                return true;
            }
            else if (scrollY < 0) {
                switchToPage(curPageNum + 1);
                return true;
            }
        }

        return super.onMouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void switchToPage(int pageNumber) {
        if (isWithinRange(pageNumber)) {
            curPageNum = pageNumber;
        }
    }

    @Override
    public int getCurrentPageNumber() {
        return curPageNum;
    }

    @Override
    public boolean isWithinRange(int pageNumber) {
        return 0 <= pageNumber && pageNumber < pages.size();
    }
}
