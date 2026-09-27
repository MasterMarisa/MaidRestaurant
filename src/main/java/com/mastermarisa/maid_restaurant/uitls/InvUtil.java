package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class InvUtil {
    /**
     * 在 [start, end) 区间内查找第一个匹配 ingredient 的槽位索引。
     *
     * @return 匹配的槽位索引；找不到返回 -1
     */
    public static int findSlot(IItemHandler handler, Ingredient ingredient, int start, int end) {
        int limit = Math.min(handler.getSlots(), end);
        for (int i = start; i < limit; ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) return i;
        }
        return -1;
    }

    /**
     * 在整个 handler 内查找第一个匹配的槽位索引。
     *
     * @return 匹配的槽位索引；找不到返回 -1
     */
    public static int findSlot(IItemHandler handler, Ingredient ingredient) {
        return findSlot(handler, ingredient, 0, handler.getSlots());
    }

    /**
     * 在 [start, end) 区间内查找所有匹配 ingredient 的槽位索引。
     * <p>
     * 返回 {@link IntList}，避免自动装箱开销。调用方用
     * {@code for (int i = 0; i < slots.size(); i++) int slot = slots.getInt(i);}
     * 遍历。
     */
    public static IntList findSlots(IItemHandler handler, Ingredient ingredient, int start, int end) {
        IntList slots = new IntArrayList();
        int limit = Math.min(handler.getSlots(), end);
        for (int i = start; i < limit; ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) slots.add(i);
        }
        return slots;
    }

    /**
     * 查找所有匹配 ingredient 的槽位索引。
     * <p>
     * 返回 {@link IntList}，避免自动装箱开销。调用方用
     * {@code for (int i = 0; i < slots.size(); i++) int slot = slots.getInt(i);}
     * 遍历。
     */
    public static IntList findSlots(IItemHandler handler, Ingredient ingredient) {
        return findSlots(handler, ingredient, 0, handler.getSlots());
    }

    /**
     * handler 内是否存在任意匹配 ingredient 的物品。
     */
    public static boolean isStackIn(IItemHandler handler, Ingredient ingredient) {
        return findSlot(handler, ingredient) != -1;
    }

    /**
     * 统计 handler 内所有匹配 ingredient 的物品总数。
     */
    public static int count(IItemHandler handler, Ingredient ingredient) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 统计列表中所有匹配 ingredient 的物品总数。
     */
    public static int count(List<ItemStack> itemStacks, Ingredient ingredient) {
        int count = 0;
        for (ItemStack itemStack : itemStacks) {
            if (ingredient.test(itemStack)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }

    /**
     * handler 内匹配 ingredient 的物品是否达到 count 个。
     */
    public static boolean contains(IItemHandler handler, Ingredient ingredient, int count) {
        if (count <= 0) return true;
        int sum = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum >= count) return true;
            }
        }
        return false;
    }

    /**
     * 列表中匹配 ingredient 的物品是否达到 count 个。
     */
    public static boolean contains(List<ItemStack> items, Ingredient ingredient, int count) {
        if (count <= 0) return true;
        int sum = 0;
        for (ItemStack stack : items) {
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum >= count) return true;
            }
        }
        return false;
    }

    /**
     * handler 与列表合并计数，是否达到 count 个。
     */
    public static boolean contains(IItemHandler handler, List<ItemStack> itemStacks,
                                   Ingredient ingredient, int count) {
        if (count <= 0) return true;
        int sum = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                sum += stack.getCount();
                if (sum >= count) return true;
            }
        }
        return contains(itemStacks, ingredient, count - sum);
    }

    /**
     * 尽可能多地提取物品，跨槽位拼接。
     * <p>
     * 若匹配到的总量不足 count，会返回已提取的部分，不保证数量达标。
     *
     * @param simulate 为 true 时只模拟，不真正提取
     * @return 提取到的物品列表；没有任何匹配则返回空列表
     */
    public static List<ItemStack> extractPartial(IItemHandler handler, int count,
                                                 Ingredient ingredient, boolean simulate) {
        if (count <= 0) return List.of();

        List<ItemStack> result = new ArrayList<>();
        int remaining = count;

        for (int i = 0; i < handler.getSlots() && remaining > 0; i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (!ingredient.test(slot)) continue;

            ItemStack extracted = handler.extractItem(i, remaining, simulate);
            if (extracted.isEmpty()) continue;

            result.add(extracted);
            remaining -= extracted.getCount();
        }
        return result;
    }

    /**
     * 跨槽位提取恰好 count 个物品。
     * <p>
     * 若匹配到的总量不足 count，返回空列表，不提取任何东西。
     *
     * @param simulate 为 true 时只模拟，不真正提取
     * @return 提取到的物品列表；无法满足数量则返回空列表
     */
    public static List<ItemStack> extractFull(IItemHandler handler, int count,
                                              Ingredient ingredient, boolean simulate) {
        if (count <= 0) return List.of();

        if (!contains(handler, ingredient, count)) {
            return List.of();
        }
        return extractPartial(handler, count, ingredient, simulate);
    }

    /**
     * 从 IMaidStorage 取物品放入 IItemHandler。
     * <p>
     * 取出的物品先尝试全部插入 to；插不进去的部分还回 from，
     * 若 from 也放不下则掉落到地面上。
     *
     * @return 是否累计转移了至少 count 个物品
     */
    public static boolean take(ServerLevel level, BlockPos pos, IMaidStorage from,
                               IItemHandler to, Ingredient ingredient, int count) {
        if (count <= 0) return true;
        if (!from.isValid(level, pos)) return false;

        List<ItemStack> extracted = from.extract(level, pos, ingredient, count, false);
        if (extracted.isEmpty()) return false;

        int remaining = count;
        for (ItemStack stack : extracted) {
            if (stack.isEmpty()) continue;

            ItemStack leftover = ItemHandlerHelper.insertItemStacked(to, stack, false);
            remaining -= stack.getCount() - leftover.getCount();

            if (!leftover.isEmpty()) {
                ItemStack back = from.insert(level, pos, leftover, false);
                if (!back.isEmpty()) {
                    ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, back);
                    level.addFreshEntity(drop);
                }
            }
        }

        return remaining <= 0;
    }

    /**
     * 把物品放入 maid 背包，放不下的部分掉落到地上。
     */
    public static void getItemToMaid(EntityMaid maid, ItemStack stack) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(maidInv, stack, false);
        if (!remainder.isEmpty()) {
            ItemEntity dropItem = maid.spawnAtLocation(remainder);
            if (dropItem != null) {
                dropItem.setPickUpDelay(0);
            }
        }
    }

    /**
     * 把原版 Inventory 里的所有物品转移到 maid 背包，并清空原容器。
     */
    public static void getAllFromInv(Inventory inventory, EntityMaid maid) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i).copy();
            if (!stack.isEmpty()) {
                inventory.setItem(i, ItemStack.EMPTY);
                getItemToMaid(maid, stack);
            }
        }
    }

    /**
     * 把 maid 背包指定槽位的物品与主手/副手物品互换。
     * <p>
     * - 槽位为空时不操作（没有可交换的物品）
     * <p>
     * - 槽位非空时，手中原物品放回背包，槽位物品置于手中
     * <p>
     * - 背包放不下的原手中物品会掉落到地上
     */
    public static void exchangeToHand(EntityMaid maid, InteractionHand hand, int index) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack fromSlot = maidInv.extractItem(index, 64, false);
        ItemStack inHand = maid.getItemInHand(hand);

        if (fromSlot.isEmpty()) return;

        maid.setItemInHand(hand, fromSlot);
        if (!inHand.isEmpty()) {
            getItemToMaid(maid, inHand);
        }
    }
}
