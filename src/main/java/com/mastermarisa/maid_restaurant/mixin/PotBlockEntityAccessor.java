package com.mastermarisa.maid_restaurant.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PotBlockEntity.class})
public interface PotBlockEntityAccessor {
    @Accessor
    Ingredient getCarrier();
}
