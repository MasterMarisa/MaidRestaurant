package com.mastermarisa.maid_restaurant.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftingTableCapability implements ICookCapability {
    public static final ResourceLocation ID = new ResourceLocation("minecraft", "crafting_table");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() { return RecipeType.CRAFTING; }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) {
        return List.of();
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        CraftingRecipe recipe = (CraftingRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) return CookResult.INTERRUPTED;

        IItemHandler maidInv = maid.getAvailableInv(false);
        CraftingContainer container = buildContainerFromHandler(maidInv, recipe, false);
        if (container == null) return CookResult.INTERRUPTED;

        List<ItemStack> remaining = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty()) continue;
            remaining.add(itemStack.getCraftingRemainingItem().copy());
        }

        maid.swing(InteractionHand.MAIN_HAND);
        ItemStack result = recipe.assemble(container, level.registryAccess());

        ItemStack leftover = ItemHandlerHelper.insertItemStacked(maidInv, result, false);
        for (ItemStack itemStack : remaining) {
            InvUtil.getItemToMaid(maid, itemStack);
        }

        if (!leftover.isEmpty()) {
            ItemEntity dropItem = maid.spawnAtLocation(leftover);
            if (dropItem != null) {
                dropItem.setPickUpDelay(0);
            }
            return CookResult.INTERRUPTED;
        }

        return node.calculateCount(level, maid) <= 0 ? CookResult.DONE :CookResult.PROGRESS;
    }

    @Override
    public int getTickInterval() {
        return 1;
    }

    /**
     * 从 handler 提取满足 recipe 的所有 ingredient，并构建可直接用于 assemble 的容器。
     * <p>
     * 分配和填充在同一过程中完成，避免二次匹配导致的不一致。
     * 失败返回 null，且不修改 handler。
     *
     * @param handler  物品源
     * @param recipe   目标配方
     * @param simulate true 时只模拟提取，不真正扣减 handler
     * @return 构建好的容器；无法满足 recipe 时返回 null
     */
    @Nullable
    public static TransientCraftingContainer buildContainerFromHandler(
            IItemHandler handler, CraftingRecipe recipe, boolean simulate) {

        List<Ingredient> ingredients = recipe.getIngredients();

        // 容器尺寸
        int width, height;
        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
        } else {
            int size = ingredients.size();
            width = Math.min(3, Math.max(1, size));
            height = Math.max(1, (size + width - 1) / width);
        }

        // 快照每个槽位的可用量
        int slots = handler.getSlots();
        ItemStack[] slotStacks = new ItemStack[slots];
        int[] available = new int[slots];
        for (int i = 0; i < slots; i++) {
            ItemStack s = handler.getStackInSlot(i);
            slotStacks[i] = s;
            available[i] = s.isEmpty() ? 0 : s.getCount();
        }

        // 为每个 ingredient 分配一个槽位（CraftingRecipe 每个槽位只需要 1 个）
        int[] slotForIngredient = new int[ingredients.size()];
        Arrays.fill(slotForIngredient, -1);

        for (int idx = 0; idx < ingredients.size(); idx++) {
            Ingredient ing = ingredients.get(idx);
            if (ing.isEmpty()) continue;

            for (int i = 0; i < slots; i++) {
                if (available[i] <= 0) continue;
                if (!ing.test(slotStacks[i])) continue;
                slotForIngredient[idx] = i;
                available[i]--;
                break;
            }

            if (slotForIngredient[idx] == -1) return null;
        }

        // 提取并填入容器
        TransientCraftingContainer container = makeCraftingContainer(width, height);
        for (int idx = 0; idx < ingredients.size(); idx++) {
            if (slotForIngredient[idx] < 0) continue;
            ItemStack extracted = handler.extractItem(slotForIngredient[idx], 1, simulate);
            if (extracted.isEmpty()) return null;
            container.setItem(idx, extracted);
        }

        return container;
    }

    public static TransientCraftingContainer makeCraftingContainer(int width, int height) {
        return new TransientCraftingContainer(
                new AbstractContainerMenu(null, -1) {
                    @Override
                    public ItemStack quickMoveStack(Player player, int index) {
                        return ItemStack.EMPTY;
                    }

                    @Override
                    public boolean stillValid(Player player) {
                        return false;
                    }
                },
                width, height
        );
    }
}
