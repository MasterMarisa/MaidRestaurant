package com.mastermarisa.maid_restaurant.core.recipe;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeCacheBuilder {
    // 配方ID -> 堆叠处理过的 Ingredient 列表
    private static final ConcurrentHashMap<ResourceLocation, List<IngredientStack>> INGREDIENT_STACK_MAP;
    // Ingredient -> 输出匹配 Ingredient 的配方ID
    public static final ConcurrentHashMap<Ingredient, List<ResourceLocation>> MATCHED_RECIPE_MAP;

    public static void buildCache(RecipeManager recipeManager, RegistryAccess registryAccess) {
        INGREDIENT_STACK_MAP.clear();
        for (var capability : CapabilityRegistry.getAll()) {
            for (Recipe<?> recipe : getAllRecipesFor(recipeManager, capability.getRecipeType())) {
                List<IngredientStack> stacks = new ArrayList<>();
                List<Ingredient> ingredients = capability.getRequiredIngredients(recipe);
                for (Ingredient ingredient : ingredients) {
                    boolean matched = false;
                    for (IngredientStack stack : stacks) {
                        if (stack.is(ingredient)) {
                            stack.setCount(stack.getCount() + 1);
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        stacks.add(new IngredientStack(ingredient));
                    }
                }
                INGREDIENT_STACK_MAP.put(recipe.getId(), stacks);
            }
        }

        MATCHED_RECIPE_MAP.clear();
        List<Recipe<?>> recipeList = Lists.newArrayList();
        for (var capability : CapabilityRegistry.getAll()) {
            recipeList.addAll(getAllRecipesFor(recipeManager, capability.getRecipeType()));
        }
        for (List<IngredientStack> stacks : INGREDIENT_STACK_MAP.values()) {
            for (IngredientStack stack : stacks) {
                List<ResourceLocation> ids = new ArrayList<>();
                for (var recipe : recipeList) {
                    if (stack.test(recipe.getResultItem(registryAccess))) {
                        ids.add(recipe.getId());
                    }
                }
                if (!ids.isEmpty()) {
                    MATCHED_RECIPE_MAP.put(stack.getIngredient(), ids);
                }
            }
        }

        MaidRestaurant.LOGGER.info("Successfully Loaded Recipe Cache With %d Entries.".formatted(MATCHED_RECIPE_MAP.size()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Recipe<?>> getAllRecipesFor(RecipeManager recipeManager, RecipeType<?> recipeType) {
        return (List<Recipe<?>>) recipeManager.getAllRecipesFor((RecipeType) recipeType);
    }

    public static List<IngredientStack> getIngredientStacks(ResourceLocation resourceLocation) {
        return INGREDIENT_STACK_MAP.getOrDefault(resourceLocation, List.of());
    }

    @Nullable
    public static IngredientStack findStack(ResourceLocation recipeId, Ingredient ingredient) {
        for (var stack : getIngredientStacks(recipeId)) {
            if (ItemUtils.equals(stack.getIngredient(), ingredient)) {
                return stack;
            }
        }
        return null;
    }

    static  {
        INGREDIENT_STACK_MAP = new ConcurrentHashMap<>();
        MATCHED_RECIPE_MAP = new ConcurrentHashMap<>();
    }
}
