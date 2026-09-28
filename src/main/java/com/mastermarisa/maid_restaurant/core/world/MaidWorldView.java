package com.mastermarisa.maid_restaurant.core.world;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;

public final class MaidWorldView implements IWorldView {
    private final IItemHandler maidInv;

    private MaidWorldView(IItemHandler maidInv) {
        this.maidInv = maidInv;
    }

    /**
     * 从女仆实体构建视图
     */
    public static MaidWorldView of(EntityMaid maid) {
        return new MaidWorldView(maid.getAvailableInv(false));
    }

    /**
     * 从IItemHandler构建视图,避免重复调用 {@code getAvailableInv}。
     */
    public static MaidWorldView of(IItemHandler maidInv) {
        return new MaidWorldView(maidInv);
    }

    @Override
    public int held(Ingredient ingredient) {
        return InvUtil.count(maidInv, ingredient);
    }
}
