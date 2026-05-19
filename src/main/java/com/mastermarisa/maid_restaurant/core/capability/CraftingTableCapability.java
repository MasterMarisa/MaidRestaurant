package com.mastermarisa.maid_restaurant.core.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.recipe.CookStep;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CraftingTableCapability implements ICookCapability {
    public static final String UID = "crafting_table";

    @Override
    public String getUID() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    @Nullable
    public BlockPos searchWorkBlock(ServerLevel level, RestaurantZone zone, EntityMaid maid) {
        if (!zone.isValid()) {
            return null;
        }
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = zone.getMin().getX(); x <= zone.getMax().getX(); x++) {
            for (int y = zone.getMin().getY(); y <= zone.getMax().getY(); y++) {
                for (int z = zone.getMin().getZ(); z <= zone.getMax().getZ(); z++) {
                    mutable.set(x, y, z);
                    if (level.getBlockState(mutable).is(Blocks.CRAFTING_TABLE)
                            && !BlockUsageUtils.isUsed(mutable)) {
                        found.add(mutable.immutable());
                    }
                }
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.stream().min(Comparator.comparingDouble(p -> p.distSqr(maid.blockPosition()))).orElse(null);
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    public List<ItemStack> getCurrentInput(ServerLevel level, BlockPos pos) {
        return List.of();
    }

    @Override
    public CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookStep step) {
        CompoundTag params = step.getParams();
        ItemStack result;
        List<Ingredient> ingredients;

        if (params.contains(CookStep.RECIPE_ID, Tag.TAG_STRING)) {
            ResourceLocation recipeId = ResourceLocation.tryParse(params.getString(CookStep.RECIPE_ID));
            if (recipeId == null) {
                return CookResult.INTERRUPTED;
            }
            Optional<? extends Recipe<?>> recipeOpt = level.getRecipeManager().byKey(recipeId);
            if (recipeOpt.isEmpty()) {
                return CookResult.INTERRUPTED;
            }
            Recipe<?> recipe = recipeOpt.get();
            result = recipe.getResultItem(level.registryAccess());
            ingredients = recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
        } else {
            return CookResult.INTERRUPTED;
        }

        if (result.isEmpty()) {
            return CookResult.INTERRUPTED;
        }

        IItemHandler maidInv = maid.getAvailableInv(false);
        if (!ItemUtils.allMatch(ingredients, maidInv)) {
            return CookResult.INTERRUPTED;
        }

        for (var ingredient : ingredients) {
            ItemUtils.tryExtract(maidInv, 1, ingredient, true, false);
        }

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(maidInv, result.copy(), false);
        if (!remainder.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), remainder);
            level.addFreshEntity(itemEntity);
        }

        maid.swing(InteractionHand.OFF_HAND);
        return CookResult.DONE;
    }

    @Override
    public boolean validate(CookStep step, Level level) {
        CompoundTag params = step.getParams();
        if (params.contains(CookStep.RECIPE_ID, Tag.TAG_STRING)) {
            ResourceLocation recipeId = ResourceLocation.tryParse(params.getString(CookStep.RECIPE_ID));
            return recipeId != null && level.getRecipeManager().byKey(recipeId).isPresent();
        }
        return false;
    }

    @Override
    public List<Ingredient> getRequiredMaterials(CookStep step, Level level) {
        CompoundTag params = step.getParams();
        NonNullList<Ingredient> ingredients = NonNullList.create();

        if (params.contains(CookStep.RECIPE_ID, Tag.TAG_STRING)) {
            ResourceLocation recipeId = ResourceLocation.tryParse(params.getString(CookStep.RECIPE_ID));
            if (recipeId != null) {
                Optional<? extends Recipe<?>> recipeOpt = level.getRecipeManager().byKey(recipeId);
                recipeOpt.ifPresent(recipe -> ingredients.addAll(recipe.getIngredients()));
            }
        }

        return ingredients.stream().filter(i -> !i.isEmpty()).toList();
    }
}
