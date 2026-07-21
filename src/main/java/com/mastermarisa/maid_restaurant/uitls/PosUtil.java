package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;

public class PosUtil {
    public static double distSqrHorizontal(EntityMaid maid, BlockPos pos) {
        double dx = maid.getX() - (pos.getX() + 0.5);
        double dz = maid.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dz * dz;
    }

    protected static boolean isEmptyBlockPos(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(
                level,
                pos,
                CollisionContext.empty()
        ).isEmpty();
    }

    public static boolean isSafePos(Level level, BlockPos pos) {
        return isEmptyBlockPos(level, pos)
                && isEmptyBlockPos(level, pos.above())
                && !isEmptyBlockPos(level, pos.below());
    }
}
