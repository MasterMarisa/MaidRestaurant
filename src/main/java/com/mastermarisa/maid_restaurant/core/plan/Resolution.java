package com.mastermarisa.maid_restaurant.core.plan;

import java.util.List;

public sealed interface Resolution {
    record Satisfied() implements Resolution {}

    record Fetch(Need need, List<SupplyTarget> targets, int amount) implements Resolution {}

    record Craft(Need need, List<Need> children, boolean ready) implements Resolution {}

    record Impossible(Need need, Reason reason) implements Resolution {}

    enum Reason {
        NO_SUPPLY,
        NO_RECIPE
    }
}
