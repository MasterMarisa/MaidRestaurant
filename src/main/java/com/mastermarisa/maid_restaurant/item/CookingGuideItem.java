package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.client.gui.screen.CookingGuideScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CookingGuideItem extends Item {
    private static final String TAG_RECIPE_ROOT = "recipe_root";

    public CookingGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide()) {
            CookingGuideScreen.open(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static CompoundTag getRecipeRoot(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(TAG_RECIPE_ROOT)) {
            return tag.getCompound(TAG_RECIPE_ROOT);
        }
        return new CompoundTag();
    }

    public static void setRecipeRoot(ItemStack itemStack, CompoundTag root) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put(TAG_RECIPE_ROOT, root);
    }
}
