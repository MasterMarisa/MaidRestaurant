package com.mastermarisa.maid_restaurant.compat.bakeries;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.*;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import com.renyigesai.bakeries.common.blocks.toaster.ToasterBlock;
import com.renyigesai.bakeries.common.blocks.toaster.ToasterBlockEntity;
import com.renyigesai.bakeries.common.init.BakeriesItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ToasterCookTask implements ICookTask {
    public static final String UID = "ToasterCookTask";

    @Override
    public String getUID() { return UID; }

    @Override
    public ItemStack getIcon() { return new ItemStack(BakeriesItems.TOASTER.get()); }

    @Override
    public RecipeType<?> getType() { return RecipeType.CAMPFIRE_COOKING; }

    @Override
    public List<ItemStack> getCurrentInput(Level level, BlockPos pos, EntityMaid maid) {
        List<ItemStack> ans = new ArrayList<>();
        CookRequest request = Objects.requireNonNull((CookRequest) RequestManager.peek(maid, CookRequest.TYPE));
        RecipeHolder<CampfireCookingRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(RecipeType.CAMPFIRE_COOKING, request.id));

        if (level.getBlockEntity(pos) instanceof ToasterBlockEntity toaster) {
            ItemStackHandler handler = toaster.getItems();
            for (int i = 0;i < 2;i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                if (recipe.value().matches(new SingleRecipeInput(itemStack), level))
                    ans.add(itemStack.copy());
                else if (!itemStack.isEmpty() && getResult(recipe,level).is(itemStack.getItem())) {
                    List<Ingredient> available = recipe.value().getIngredients().stream().filter(s -> !s.isEmpty() && s.getItems().length > 0).toList();
                    if (!available.isEmpty())
                        ans.add(available.getFirst().getItems()[0].copy());
                }
            }
        }

        return ans;
    }

    @Override
    public @Nullable BlockPos searchWorkBlock(ServerLevel level, EntityMaid maid, int horizontalSearchRange, int verticalSearchRange) {
        BlockPos center = maid.getBrainSearchPos();
        List<BlockPos> foundOvens = SearchUtils.search(center,horizontalSearchRange,verticalSearchRange, pos ->
                level.getBlockEntity(pos) instanceof ToasterBlockEntity && BlockUsageManager.getUserCount(pos) <= 0
        );

        if (!foundOvens.isEmpty()) {
            return foundOvens.stream().min(Comparator.comparingDouble(p->p.distSqr(maid.blockPosition()))).get();
        }

        return null;
    }

    @Override
    public boolean isValidWorkBlock(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ToasterBlockEntity;
    }

    @Override
    public void cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookRequest request) {
        ToasterBlockEntity toaster = Objects.requireNonNull((ToasterBlockEntity) level.getBlockEntity(pos));
        ItemStackHandler handler = toaster.getItems();
        RecipeHolder<CampfireCookingRecipe> recipe = Objects.requireNonNull(level.getRecipeManager().byKeyTyped(RecipeType.CAMPFIRE_COOKING, request.id));
        List<Ingredient> available = recipe.value().getIngredients().stream().filter(s -> !s.isEmpty() && s.getItems().length > 0).toList();
        if (available.isEmpty()) return;

        int count = 0;
        boolean resultFound = false;
        boolean added = false;
        for (int i = 0;i < 2;i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            int requested = request.extraData.getInt("left");
            if (itemStack.isEmpty()) {
                count++;
                if (requested < request.requested) {
                    ItemStack ingredient = ItemHandlerUtils.tryExtractSingleSlot(maid.getAvailableInv(false),1, StackPredicate.of(available.get(0)),true);
                    if (!ingredient.isEmpty()) {
                        handler.setStackInSlot(i,ingredient);
                        toaster.getCookingProgress()[i] = 0;
                        toaster.getCookingTime()[i] = recipe.value().getCookingTime();
                        toaster.setChanged();
                        maid.swing(InteractionHand.OFF_HAND);
                        requested++;
                        request.extraData.putInt("left",requested);
                        added = true;
                    }
                }
            } else if (request.remain > 0 && itemStack.is(getResult(recipe,level).getItem())) {
                if (ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false),itemStack.copy(),false).isEmpty()) {
                    handler.setStackInSlot(i,ItemStack.EMPTY);
                    toaster.getCookingProgress()[i] = 0;
                    toaster.getCookingTime()[i] = 0;
                    toaster.setChanged();
                    toaster.updateBlock();
                    maid.swing(InteractionHand.OFF_HAND);
                    request.remain--;
                    resultFound = true;
                }
            }
        }

        int requested = request.extraData.getInt("left");
        if ((count == 0 || (requested >= request.requested && added)) && !resultFound) {
            toaster.changeState(0);
            level.setBlock(pos, (BlockState)level.getBlockState(pos).setValue(ToasterBlock.STATE, ToasterBlock.State.LIT), 3);
        }
    }

    @Override
    public List<RecipeData> getAllRecipeData(Level level) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeData> ans = new ArrayList<>();
        for (var holder : manager.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING)) {
            ans.add(new RecipeData(holder.id(), getType(), getIcon(), getResult(holder,level)));
        }

        return ans;
    }

    public static void register() {
        CookTasks.register(new ToasterCookTask());
    }
}
