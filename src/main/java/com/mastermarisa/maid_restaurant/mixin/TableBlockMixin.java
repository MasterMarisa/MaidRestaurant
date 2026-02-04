package com.mastermarisa.maid_restaurant.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.TableBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.mastermarisa.maid_restaurant.data.TagItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {TableBlock.class},remap = false)
public class TableBlockMixin {
    @Inject(method = "useWithOther", at = @At("HEAD"), cancellable = true)
    private void useWithOther(Level level, BlockPos pos, Player player, InteractionHand hand, TableBlockEntity table, ItemStack itemInHand, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (itemInHand.is(TagItem.TABLE_BLACKLIST)) {
            cir.setReturnValue(ItemInteractionResult.FAIL);
            cir.cancel();
        }
    }
}
