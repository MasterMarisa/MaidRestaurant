package com.mastermarisa.maid_restaurant.core.world;

/**
 * 规划期的只读世界上下文
 *
 * @param items   物品就绪量视图
 * @param recipes 配方查询
 */
public record WorldContext(IWorldView items, IRecipeLookup recipes) {
}
