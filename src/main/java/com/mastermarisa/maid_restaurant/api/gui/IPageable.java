package com.mastermarisa.maid_restaurant.api.gui;

public interface IPageable {
    void switchToPage(int pageNumber);

    int getCurrentPageNumber();

    boolean isWithinRange(int pageNumber);
}
