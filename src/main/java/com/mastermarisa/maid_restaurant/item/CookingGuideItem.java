package com.mastermarisa.maid_restaurant.item;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.screen.CookingGuideScreen;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class CookingGuideItem extends Item {
    public static final ResourceLocation HAS_RECIPE_PROPERTY = MaidRestaurant.modLoc("has_recipe");
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

    public static boolean hasRecipe(ItemStack stack) {
        CompoundTag tag = getRecipeRoot(stack);
        if (tag.isEmpty()) {
            return false;
        }

        if (!tag.contains("count") || !tag.contains("ingredient")) {
            return false;
        }

        int count = tag.getInt("count");
        if (count <= 0) {
            return false;
        }

        byte[] bytes = tag.getByteArray("ingredient");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        Ingredient ingredient = Ingredient.fromNetwork(buffer);

        return !ingredient.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public static float getTexture(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return hasRecipe(stack) ? 1.0F : 0.0F;
    }
}
