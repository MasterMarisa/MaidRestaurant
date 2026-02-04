package com.mastermarisa.maid_restaurant.uitls;

public enum TargetType {
    COOK_BLOCK(0),
    STORAGE_BLOCK(1),
    SERVE_TABLE_POS(4),
    DROP_MEAL_POS(5),
    COOKER_POS(6);

    private TargetType(int type) { this.type = type; }
    public final int type;
}
