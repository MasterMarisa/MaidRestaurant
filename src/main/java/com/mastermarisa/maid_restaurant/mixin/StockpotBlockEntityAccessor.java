package com.mastermarisa.maid_restaurant.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({StockpotBlockEntity.class})
public interface StockpotBlockEntityAccessor {
    @Accessor
    void setSoupBaseId(ResourceLocation soupBaseId);
    @Accessor
    void setStatus(int status);
}
