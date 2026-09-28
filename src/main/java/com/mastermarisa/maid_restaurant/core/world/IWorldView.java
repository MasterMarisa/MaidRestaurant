package com.mastermarisa.maid_restaurant.core.world;

import net.minecraft.world.item.crafting.Ingredient;

public interface IWorldView {
    /**
     * 统计当前已就绪的匹配物品总数
     *
     * @param ingredient 匹配条件
     * @return 匹配的物品总数量
     */
    int held(Ingredient ingredient);
}
