package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.UnboundMenuScreen;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.init.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnboundMenuItem extends Item {
    private static final String TAG_INDEX_LIST = "index_list";
    private static final String TAG_ENTRY_LIST = "entry_list";

    public UnboundMenuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.isSecondaryUseActive()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ModItems.RESTAURANT_MENU.get().getDefaultInstance());
            Map<Integer, MenuEntry> map = UnboundMenuItem.getEntries(stack);
            UnboundMenuItem.setEntries(player.getMainHandItem(), map);
            return InteractionResultHolder.success(stack);
        }

        if (level.isClientSide()) {
            UnboundMenuScreen.open(stack, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static Map<Integer, MenuEntry> getEntries(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        Int2ObjectLinkedOpenHashMap<MenuEntry> map = new Int2ObjectLinkedOpenHashMap<>();
        if (tag.contains(TAG_ENTRY_LIST) && tag.contains(TAG_INDEX_LIST)) {
            int[] indexList = tag.getIntArray(TAG_INDEX_LIST);
            ListTag entryList = tag.getList(TAG_ENTRY_LIST, Tag.TAG_COMPOUND);
            if (indexList.length == entryList.size()) {
                for (int i = 0; i < indexList.length; i++) {
                    MenuEntry entry = new MenuEntry();
                    entry.deserializeNBT(entryList.getCompound(i));
                    map.put(indexList[i], entry);
                }
            }
        }
        return map;
    }

    public static void setEntries(ItemStack itemStack, Map<Integer, MenuEntry> map) {
        CompoundTag tag = itemStack.getOrCreateTag();
        List<Integer> indexList = new ArrayList<>();
        ListTag entryList = new ListTag();
        for (var entry : map.entrySet()) {
            indexList.add(entry.getKey());
            entryList.add(entry.getValue().serializeNBT());
        }
        tag.putIntArray(TAG_INDEX_LIST, indexList);
        tag.put(TAG_ENTRY_LIST, entryList);
    }
}
