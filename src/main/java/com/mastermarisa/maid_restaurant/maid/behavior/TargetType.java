package com.mastermarisa.maid_restaurant.maid.behavior;

public enum TargetType {
    GATHER_MATERIAL(0),
    APPROACH_WORK_BLOCK(1),
    EXECUTE_COOK_STEP(2),
    STORE_DISH(3),
    PICKUP_DISH(4),
    SERVE_DISH(5);

    TargetType(int id) {
        this.id = id;
    }

    public final int id;
}
