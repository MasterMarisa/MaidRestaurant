package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.blockentity.OrderBellBlockEntity;
import com.mastermarisa.maid_restaurant.data.request.ServeRequest;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.CodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OrderBellItem extends BlockItem {
    private static final String TAG_TARGETS = "targets";

    public OrderBellItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) {
            return super.useOn(context);
        }
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            pos = pos.relative(direction);
        }

        List<ServeRequest.Target> targets = getTargets(stack);
        boolean removed = false;
        for (int i = 0; i < targets.size(); i++) {
            ServeRequest.Target target = targets.get(i);
            if (target.pos().equals(pos)) {
                targets.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            int type = StorageRegistry.tryGetAt(level, pos) != null ? 0 : 1;
            targets.add(new ServeRequest.Target(pos, type));
        }
        setTargets(stack, targets);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof OrderBellBlockEntity be) {
            List<ServeRequest.Target> targets = getTargets(stack);
            be.setTargets(targets);
        }
        stack.removeTagKey(TAG_TARGETS);

        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || isSelected || level.getGameTime() % 7 != 0) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_TARGETS)) {
            tag.remove(TAG_TARGETS);
        }
    }

    public static List<ServeRequest.Target> getTargets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        List<ServeRequest.Target> targets = new ArrayList<>();
        if (tag.contains(TAG_TARGETS)) {
            ListTag listTag = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                targets.add(CodecUtil.deserialize(listTag.getCompound(i), ServeRequest.Target.CODEC));
            }
        }
        return targets;
    }

    public static void setTargets(ItemStack itemStack, List<ServeRequest.Target> targets) {
        CompoundTag tag = itemStack.getOrCreateTag();
        ListTag listTag = new ListTag();
        for (var Target : targets) {
            listTag.add(CodecUtil.serialize(Target, ServeRequest.Target.CODEC));
        }
        tag.put(TAG_TARGETS, listTag);
    }
}
