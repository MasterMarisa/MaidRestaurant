package com.mastermarisa.maid_restaurant.core.plan;

import net.minecraft.core.BlockPos;

/**
 * 一个可提供原料的外部位置
 *
 * @param pos       容器位置
 * @param available 可提供数量(已按需求量截断)
 */
public record SupplyTarget(BlockPos pos, int available) {
}
