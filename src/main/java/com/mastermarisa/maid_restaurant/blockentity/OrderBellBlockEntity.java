package com.mastermarisa.maid_restaurant.blockentity;

import com.mastermarisa.maid_restaurant.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OrderBellBlockEntity extends BaseBlockEntity {
    public AnimationState shakingState;

    public OrderBellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ORDER_BELL_BE.get(), pos, state);
        this.shakingState = new AnimationState();
    }

    public void animate(Level level) {
        this.shakingState.start((int) level.getGameTime());
    }
}
