package com.mastermarisa.maid_restaurant.integration.bakeries.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.capability.CookResult;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.InvUtil;
import com.renyigesai.bakeries.init.BakeriesBlocks;
import com.renyigesai.bakeries.init.BakeriesItems;
import com.renyigesai.bakeries.recipe.DoughCraftingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class DoughCraftingTableCapability implements ICookCapability {
    public static final ResourceLocation ID = new ResourceLocation("bakeries", "dough_crafting_table");

    public static void register() {
        CapabilityRegistry.register(new DoughCraftingTableCapability());
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public ItemStack getIcon() { return BakeriesItems.DOUGH_CRAFTING_TABLE.get().getDefaultInstance(); }

    @Override
    public RecipeType<?> getRecipeType() { return DoughCraftingRecipe.Type.INSTANCE; }

    @Override
    public List<ItemStack> getExistedInputs(ServerLevel level, BlockPos pos, RecipeNode node) { return List.of(); }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(BakeriesBlocks.DOUGH_CRAFTING_TABLE.get());
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, RecipeNode node) {
        DoughCraftingRecipe recipe = (DoughCraftingRecipe) node.getRecipe(level.getRecipeManager());
        if (recipe == null) {
            return CookResult.INTERRUPTED;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack result = recipe.getResultItem(level.registryAccess());
        Ingredient ingredient = recipe.getIngredients().get(0);
        int required = (int) Math.ceil((double) node.calculateCount(level, maid) / result.getCount());

        if (InvUtil.tryExtract(maidInv, required, ingredient, true, false).isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        InvUtil.getItemToMaid(maid, result.copyWithCount(result.getCount() * required));
        return CookResult.DONE;
    }

    @Override
    public int getTickInterval() {
        return 1;
    }
}
