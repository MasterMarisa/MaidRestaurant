package com.mastermarisa.maid_restaurant.core.plan;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface ISupplySource {
    /**
     * @param want 期望数量
     * @return 可提供该原料的位置
     */
    List<SupplyTarget> find(Ingredient ingredient, int want);
}
