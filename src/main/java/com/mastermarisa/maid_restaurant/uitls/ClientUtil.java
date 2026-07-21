package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ClientUtil {
    @Nullable
    public static BlockPos getTargetedBlock() {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType().equals(HitResult.Type.BLOCK)
                && hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            Direction direction = hit.getDirection();
            if (direction == Direction.UP || direction == Direction.DOWN) {
                pos = pos.relative(direction);
            }
            return pos;
        }
        return null;
    }
}
