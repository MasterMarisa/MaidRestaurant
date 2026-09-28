package com.mastermarisa.maid_restaurant.core.plan;

import com.mastermarisa.maid_restaurant.core.world.WorldContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 一次单节点规划的输入,由该节点的父节点装配
 *
 * @param parentCount     父节点推导出的需求量 (根节点为 0)
 * @param world           只读世界上下文
 * @param inPlace         父节点配方对应的、工作方块内已就位的材料 (供当前需求抵扣)
 * @param childrenInPlace 本节点配方对应的、工作方块内已就位的材料 (供子需求抵扣)
 * @param supply          外部供给查询,可为 null 表示没有
 */
public record PlanInput(int parentCount, WorldContext world, List<ItemStack> inPlace,
                        List<ItemStack> childrenInPlace, @Nullable ISupplySource supply) {
    public PlanInput {
        inPlace = List.copyOf(inPlace);
        childrenInPlace = List.copyOf(childrenInPlace);
    }
}
