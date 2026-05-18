package com.mastermarisa.maid_restaurant.maid.behavior;

public enum TargetType {
    GATHER_MATERIAL(0),
    APPROACH_WORK_BLOCK(1),
    EXECUTE_COOK_STEP(2);

    TargetType(int id) {
        this.id = id;
    }

    public final int id;
}
