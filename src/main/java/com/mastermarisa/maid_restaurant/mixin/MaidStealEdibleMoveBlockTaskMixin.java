package com.mastermarisa.maid_restaurant.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidStealEdibleMoveBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.MaidEdibleBlockAction;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin({MaidStealEdibleMoveBlockTask.class})
public class MaidStealEdibleMoveBlockTaskMixin extends MaidMoveToBlockTask {
    @Shadow
    private final MemoryModuleType<MaidEdibleBlockAction> action;

    @Shadow
    @Nullable
    private ItemStack placedStack;

    public MaidStealEdibleMoveBlockTaskMixin(float movementSpeed) {
        super(movementSpeed, 2);
        this.setMaxCheckRate(900);
        this.action = InitEntities.MAID_EDIBLE_BLOCK_ACTION.get();
    }

    /**
     * @author MasterMarisa
     * @reason DisableEdibleBlockPlacingOnTable
     */
    @Overwrite
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        Optional<MaidEdibleBlockAction> memory = maid.getBrain().getMemory(this.action);
        if (memory.isPresent() && memory.get() == MaidEdibleBlockAction.TRY_STEAL) {
            CombinedInvWrapper inv = maid.getAvailableInv(true);
            maid.getBrain().setMemory(this.action, MaidEdibleBlockAction.TRY_STEAL);
        } else {
            maid.getBrain().setMemory(this.action, MaidEdibleBlockAction.TRY_STEAL);
        }

        this.searchForDestination(worldIn, maid);
    }

    @Shadow
    protected boolean shouldMoveTo(ServerLevel worldIn, EntityMaid maid, BlockPos pos) {
        return false;
    }
}
