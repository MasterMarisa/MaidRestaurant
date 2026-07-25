package com.mastermarisa.maid_restaurant.integration.rs.storage;

import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.storage.StorageRegistry;
import com.refinedmods.refinedstorage.RSItems;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.network.node.INetworkNodeManager;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RSStorage implements IMaidStorage {
    public static final ResourceLocation ID = new ResourceLocation("refinedstorage", "terminal");

    public static void register() {
        StorageRegistry.register(new RSStorage());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return RSItems.GRID.get(DyeColor.GRAY).get().getDefaultInstance(); }

    @Override
    public boolean isValid(Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel && getNetwork(serverLevel, pos) != null;
    }

    @Override
    public List<ItemStack> extract(Level level, BlockPos pos, Ingredient ingredient, int amount, boolean simulate) {
        List<ItemStack> results = new ArrayList<>();
        if (level instanceof ServerLevel serverLevel) {
            INetwork network = getNetwork(serverLevel, pos);
            if (network != null) {
                for (ItemStack key : ingredient.getItems()) {
                    ItemStack extracted = network.extractItem(key, amount, Action.SIMULATE);
                    if (extracted.isEmpty()) continue;
                    results.add(extracted);
                    if (!simulate) {
                        network.extractItem(key, extracted.getCount(), Action.PERFORM);
                    }
                    amount -= extracted.getCount();
                    if (amount <= 0)
                        break;
                }
            }
        }
        return results;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack, boolean simulate) {
        if (level instanceof ServerLevel serverLevel) {
            INetwork network = getNetwork(serverLevel, pos);
            if (network != null) {
                Action action = simulate ? Action.SIMULATE : Action.PERFORM;
                return network.insertItem(stack, stack.getCount(), action);
            }
        }
        return stack;
    }

    @Override
    public int count(Level level, BlockPos pos, Ingredient ingredient) {
        if (level instanceof ServerLevel serverLevel) {
            INetwork network = getNetwork(serverLevel, pos);
            if (network != null) {
                IStackList<ItemStack> stackList = network.getItemStorageCache().getList();
                int total = 0;
                for (ItemStack key : ingredient.getItems()) {
                    total += stackList.getCount(key);
                }
                return total;
            }
        }
        return 0;
    }

    @Nullable
    private INetwork getNetwork(ServerLevel level, BlockPos pos) {
        INetworkNodeManager networkNodeManager = API.instance().getNetworkNodeManager(level);
        INetworkNode node = networkNodeManager.getNode(pos);
        if (node != null) {
            return node.getNetwork();
        }
        return null;
    }
}
