package com.mastermarisa.maid_restaurant.compat.farmersdelight;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.uitls.BlockPosUtils;
import com.mastermarisa.maid_restaurant.uitls.MaidInvUtils;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import com.mastermarisa.maid_restaurant.uitls.StackPredicate;
import com.mastermarisa.maid_restaurant.uitls.manager.BlockUsageManager;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import com.mastermarisa.maid_restaurant.uitls.manager.InitializationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CookingPotCookTask implements ICookTask {
    public static final String UID = "CookingPotCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(ModItems.COOKING_POT.get()); }

    @Override
    public RecipeType<?> getType() { return ModRecipeTypes.COOKING.get(); }

    @Override
    public List<StackPredicate> getIngredients(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        CookingPotRecipe recipe = (CookingPotRecipe) recipeHolder.value();
        List<StackPredicate> predicates = new ArrayList<>(recipe.getIngredients().stream().filter(s->!s.isEmpty()).map(StackPredicate::new).toList());
        if (!recipe.getOutputContainer().isEmpty())
            predicates.add(StackPredicate.of(recipe.getOutputContainer()));

        return predicates;
    }

    @Override
    public List<StackPredicate> getKitchenWares() {
        return List.of();
    }

    @Override
    public ItemStack getResult(RecipeHolder<? extends Recipe<?>> recipeHolder) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return recipeHolder.value().getResultItem(Minecraft.getInstance().level.registryAccess());
        } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            return recipeHolder.value().getResultItem(InitializationHelper.getServerCache().registryAccess());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundPots = BlockPosUtils.search(center,horizontalSearchRange,verticalSearchRange,pos ->
           level.getBlockEntity(pos) instanceof CookingPotBlockEntity && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundPots.isEmpty()) {
            return foundPots.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.isHeated();
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        CookingPotBlockEntity pot = Objects.requireNonNull((CookingPotBlockEntity) level.getBlockEntity(pos));
        ItemStackHandler handler = pot.getInventory();

        ItemStack meal = pot.getMeal();
        ItemStack container = handler.getStackInSlot(7);
        ItemStack result = handler.getStackInSlot(8);
        RecipeHolder<? extends Recipe<?>> holder = RecipeUtils.byKeyTyped(request.type,request.id);
        CookingPotRecipe recipe = (CookingPotRecipe) holder.value();
        if (!result.isEmpty() && result.is(getResult(holder).getItem())) {
            if (result.getCount() >= request.count) {
                ItemUtils.getItemToLivingEntity(maid,handler.extractItem(8,request.count,false));
                request.count = 0;
            } else {
                request.count -= result.getCount();
                ItemUtils.getItemToLivingEntity(maid,handler.extractItem(8,result.getCount(),false));
            }
        } else if (!meal.isEmpty() && container.isEmpty()) {
            ItemStack carrier = MaidInvUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,StackPredicate.of(recipe.getOutputContainer().getItem()),true);
            if (!carrier.isEmpty()) {
                handler.setStackInSlot(7,carrier);
            }
        } else {
            List<ItemStack> slots = new ArrayList<>();
            for (int i = 0;i < 6;i++) slots.add(handler.getStackInSlot(i));
            List<StackPredicate> required = getIngredients(holder);
            required = MaidInvUtils.getRequired(required,slots);
            for (StackPredicate predicate : required) {
                if (!recipe.getOutputContainer().isEmpty() && predicate.test(recipe.getOutputContainer())) continue;
                List<Integer> indexs = getEmptySlots(slots);
                if (!indexs.isEmpty()) {
                    ItemStack material = MaidInvUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1,predicate,true);
                    handler.setStackInSlot(indexs.getFirst(),material);
                }
            }
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData() {
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : RecipeUtils.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COOKING.get())) {
            ans.add(new RecipeData(holder.id(),ModRecipeTypes.COOKING.get(),getIcon(),getResult(holder)));
        }

        return ans;
    }

    private List<Integer> getEmptySlots(List<ItemStack> slots) {
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0;i < slots.size();i++)
            if (slots.get(i).isEmpty())
                indexs.add(i);

        return indexs;
    }

    public static void register() {
        CookTaskManager.register(new CookingPotCookTask());
    }
}
