package com.mastermarisa.maid_restaurant.block;

import com.mastermarisa.maid_restaurant.uitls.VoxelShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

@SuppressWarnings("deprecation")
public class HorizontalDirectionalOnlyBlock extends HorizontalDirectionalBlock {
    @Nullable
    private final EnumMap<Direction, VoxelShape> shapes;

    public HorizontalDirectionalOnlyBlock(Properties properties, @Nullable VoxelShape shape) {
        super(properties);
        shapes = shape != null ? VoxelShapeUtil.horizontalShapes(shape) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes != null ? shapes.get(state.getValue(FACING)) : super.getShape(state, level, pos, context);
    }
}
