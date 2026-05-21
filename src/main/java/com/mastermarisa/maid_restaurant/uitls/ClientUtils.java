package com.mastermarisa.maid_restaurant.uitls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ClientUtils {
    @Nullable
    public static ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Nullable
    public static BlockPos getTargetedBlock() {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType().equals(HitResult.Type.BLOCK) && hitResult instanceof BlockHitResult hit) {
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
