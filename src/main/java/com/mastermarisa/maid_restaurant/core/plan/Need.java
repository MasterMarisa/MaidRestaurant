package com.mastermarisa.maid_restaurant.core.plan;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * @param ingredient 匹配条件
 * @param amount     缺口数量
 */
public record Need(Ingredient ingredient, int amount) {
}
