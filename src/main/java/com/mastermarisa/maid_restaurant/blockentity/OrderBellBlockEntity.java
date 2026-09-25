package com.mastermarisa.maid_restaurant.blockentity;

import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.init.ModBlocks;
import com.mastermarisa.maid_restaurant.init.ModSounds;
import com.mastermarisa.maid_restaurant.uitls.CodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OrderBellBlockEntity extends BaseBlockEntity {
    private static final String TAG_TARGETS = "targets";

    private List<ServeRequest.Target> targets;
    public AnimationState shakingState;

    public OrderBellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ORDER_BELL_BE.get(), pos, state);
        this.shakingState = new AnimationState();
    }

    public void animate(Level level) {
        this.shakingState.start((int) level.getGameTime());
        level.playSound(null, this.worldPosition, ModSounds.ORDER_BELL.get(), SoundSource.BLOCKS, 1, 1);
    }

    public void setTargets(List<ServeRequest.Target> targets) {
        this.targets = targets;
        this.setChanged();
    }

    @Nullable
    public List<ServeRequest.Target> getTargets() {
        return this.targets;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.targets != null) {
            ListTag listTag = new ListTag();
            for (ServeRequest.Target target : targets) {
                listTag.add(CodecUtil.serialize(target, ServeRequest.Target.CODEC));
            }
            tag.put(TAG_TARGETS, listTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_TARGETS, Tag.TAG_LIST)) {
            this.targets.clear();
            ListTag listTag = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                this.targets.add(CodecUtil.deserialize(listTag.getCompound(i), ServeRequest.Target.CODEC));
            }
        }
    }
}
