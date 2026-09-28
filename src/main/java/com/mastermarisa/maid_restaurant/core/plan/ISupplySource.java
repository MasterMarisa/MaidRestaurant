package com.mastermarisa.maid_restaurant.core.plan;

import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.List;

public interface ISupplySource {
    /**
     * 全量查询,结果按取用优先级排列
     *
     * @param want 期望数量
     * @return 可提供该原料的位置
     */
    List<SupplyTarget> find(Ingredient ingredient, int want);

    /**
     * 只取最优先的一个位置
     * <p>
     * 默认实现使用 {@link #find} (扫描全部容器); 能在命中后提前退出的实现应覆写该方法
     *
     * @return 没有则返回 null
     */
    @Nullable
    default SupplyTarget findFirst(Ingredient ingredient) {
        List<SupplyTarget> targets = find(ingredient, Integer.MAX_VALUE);
        return targets.isEmpty() ? null : targets.get(0);
    }
}
