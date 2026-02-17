package com.mastermarisa.maid_restaurant.client.gui.base;

import com.mastermarisa.maid_restaurant.api.gui.IPageable;

import java.awt.*;

public abstract class UIPageable extends UIElement implements IPageable {
    protected int curPageNum;

    public UIPageable(Rectangle frame) {
        super(frame);
    }

    @Override
    protected boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!frame.contains(mouseX,mouseY) || scrollY == 0) return super.onMouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (scrollY > 0) switchToPage(curPageNum - 1);
        else switchToPage(curPageNum + 1);
        return true;
    }

    @Override
    public final void switchToPage(int pageNumber) {
        if (isWithinRange(pageNumber)) {
            curPageNum = pageNumber;
            onSwitchedTo(pageNumber);
        }
    }

    @Override
    public int getCurrentPageNumber() {
        return curPageNum;
    }

    protected abstract void onSwitchedTo(int pageNumber);
}
