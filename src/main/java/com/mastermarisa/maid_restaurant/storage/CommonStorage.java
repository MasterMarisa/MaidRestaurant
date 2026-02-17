package com.mastermarisa.maid_restaurant.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommonStorage implements IMaidStorage {
    public static final String UID = "CommonStorage";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Items.CHEST); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return getHandler(level,pos) != null;
    }

    @Override
    public @Nullable IItemHandler getHandler(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TagBlock.STORAGE_BLOCK)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, level.getBlockState(pos), blockEntity, null);
        }

        return null;
    }

    @Override
    public ItemStack extract(Level level, BlockPos pos, int slot, int amount, boolean simulate) {
        IItemHandler handler = getHandler(level,pos);
        if (handler != null) return handler.extractItem(slot,amount,simulate);

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        IItemHandler handler = getHandler(level,pos);
        if (handler != null) return ItemHandlerHelper.insertItemStacked(handler,stack,simulate);

        return stack;
    }
}
