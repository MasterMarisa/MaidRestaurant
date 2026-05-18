package com.mastermarisa.maid_restaurant.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.core.capability.CookResult;
import com.mastermarisa.maid_restaurant.core.recipe.CookStep;
import com.mastermarisa.maid_restaurant.core.zone.RestaurantZone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public interface ICookCapability {
    String getUID();

    ItemStack getIcon();

    @Nullable
    BlockPos searchWorkBlock(ServerLevel level, RestaurantZone zone, EntityMaid maid);

    boolean isValidWorkBlock(ServerLevel level, BlockPos pos);

    CookResult cookTick(ServerLevel level, EntityMaid maid, BlockPos pos, CookStep step);

    List<ItemStack> getCurrentInput(ServerLevel level, BlockPos pos);

    boolean validate(CookStep step, Level level);

    List<Ingredient> getRequiredMaterials(CookStep step, Level level);

    default List<Ingredient> getRequiredTools(CookStep step, Level level) { return List.of(); }
}
