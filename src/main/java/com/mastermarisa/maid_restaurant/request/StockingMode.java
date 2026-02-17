package com.mastermarisa.maid_restaurant.request;

public enum StockingMode {
    DISABLED(0),
    INSERTABLE(1),
    SPACE_ENOUGH(2);

    private StockingMode(int id) { this.id = id; }

    public final int id;
}
