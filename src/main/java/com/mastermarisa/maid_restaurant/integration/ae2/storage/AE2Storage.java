package com.mastermarisa.maid_restaurant.integration.ae2.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.parts.reporting.AbstractTerminalPart;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AE2Storage implements IMaidStorage {
    public static final ResourceLocation ID = new ResourceLocation("ae2", "terminal");

    public static void register() {
        StorageRegistry.register(new AE2Storage());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return AEItems.ITEM_CELL_256K.stack(); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CableBusBlockEntity cbb) {
            return Arrays.stream(Direction.values())
                    .anyMatch(direction -> {
                        IPart part = cbb.getCableBus().getPart(direction);
                        return part instanceof AbstractTerminalPart;
                    });
        }
        return false;
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        List<ItemStack> results = new ArrayList<>();
        MEStorage inv = getInv(level, pos);
        if (inv != null) {
            List<AEItemKey> keys = Arrays.stream(ingredient.getItems()).map(AEItemKey::of).toList();
            for (AEItemKey key : keys) {
                long extract = inv.extract(key, amount, Actionable.SIMULATE, IActionSource.empty());
                if (extract == 0) continue;
                while (extract > 0) {
                    int scheduled = (int) Math.min(extract, key.getReadOnlyStack().getMaxStackSize());
                    if (scheduled != 0) {
                        results.add(key.getReadOnlyStack().copyWithCount(scheduled));
                        if (!simulate) {
                            inv.extract(key, scheduled, Actionable.MODULATE, IActionSource.empty());
                        }
                        extract -= scheduled;
                        amount -= scheduled;
                    } else break;
                }
                if (amount <= 0)
                    break;
            }
        }
        return results;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        MEStorage inv = getInv(level, pos);
        if (inv != null) {
            AEItemKey key = AEItemKey.of(stack);
            if (key != null) {
                Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                long insert = inv.insert(key, stack.getCount(), action, IActionSource.empty());
                ItemStack result = stack.copy();
                result.shrink((int) insert);
                return result;
            }
        }
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        MEStorage inv = getInv(level, pos);
        if (inv != null) {
            List<AEItemKey> keys = Arrays.stream(ingredient.getItems()).map(AEItemKey::of).toList();
            KeyCounter counter = inv.getAvailableStacks();
            long total = 0;

            for (AEItemKey key : keys) {
                total += counter.get(key);
                if (total > Integer.MAX_VALUE) {
                    total = Integer.MAX_VALUE;
                    break;
                }
            }
            return Math.toIntExact(total);
        }
        return 0;
    }

    @Nullable
    private MEStorage getInv(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CableBusBlockEntity cbbe) {
            Optional<Direction> first = Arrays.stream(Direction.values())
                    .filter(direction -> {
                        IPart part = cbbe.getCableBus().getPart(direction);
                        return part instanceof AbstractTerminalPart;
                    })
                    .findFirst();

            if (first.isEmpty()) {
                return null;
            }

            IGridNode terminal = cbbe.getGridNode(first.get());
            if (terminal != null) {
                IGrid grid = terminal.getGrid();
                if (grid != null) {
                    return grid.getStorageService().getInventory();
                }
            }
        }
        return null;
    }
}
