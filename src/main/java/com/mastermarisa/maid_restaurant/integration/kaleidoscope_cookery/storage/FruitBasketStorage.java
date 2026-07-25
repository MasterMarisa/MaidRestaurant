package com.mastermarisa.maid_restaurant.integration.kaleidoscope_cookery.storage;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class FruitBasketStorage implements IMaidStorage {
    public static final ResourceLocation ID = new ResourceLocation("kaleidoscope_cookery", "fruit_basket");

    public static void register() {
        StorageRegistry.register(new FruitBasketStorage());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() {
        return ModItems.FRUIT_BASKET.get().getDefaultInstance();
    }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FruitBasketBlockEntity;
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity be) {
            List<ItemStack> result = InvUtil.tryExtract(be.getItems(), amount, ingredient, false, simulate);
            if (!simulate) {
                be.refresh();
            }
            return result;
        }
        return List.of();
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity be) {
            if (stack.getItem().canFitInsideContainerItems()) {
                ItemStack result = ItemHandlerHelper.insertItemStacked(be.getItems(), stack, simulate);
                if (!simulate) {
                    be.refresh();
                }
                return result;
            }
        }
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        if (level.getBlockEntity(pos) instanceof FruitBasketBlockEntity be) {
            return InvUtil.count(be.getItems(), ingredient);
        }
        return 0;
    }
}
