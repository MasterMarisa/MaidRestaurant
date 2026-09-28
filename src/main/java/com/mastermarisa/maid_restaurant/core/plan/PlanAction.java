package com.mastermarisa.maid_restaurant.core.plan;

import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;

public record PlanAction(ExecutionNode node, Resolution resolution) {
}
