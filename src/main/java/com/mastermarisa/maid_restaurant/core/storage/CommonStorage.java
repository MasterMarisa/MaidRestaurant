package com.mastermarisa.maid_restaurant.core.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.init.tag.TagMod;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class CommonStorage implements IMaidStorage {
    public static final String UID = "CommonStorage";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Items.CHEST); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return getItemHandler(level,pos) != null;
    }

    @Override
    public ItemStack extract(Level level, BlockPos pos, int slot, int amount, boolean simulate) {
        IItemHandler handler = getItemHandler(level,pos);
        if (handler != null) {
            return handler.extractItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        IItemHandler handler = getItemHandler(level,pos);
        if (handler != null) {
            return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
        }
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        IItemHandler handler = getItemHandler(level, pos);
        if (handler == null) {
            return 0;
        }
        return ItemUtils.count(handler, ingredient);
    }

    @Nullable
    private IItemHandler getItemHandler(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TagMod.STORAGE_BLOCK)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
            }
        }
        return null;
    }
}
