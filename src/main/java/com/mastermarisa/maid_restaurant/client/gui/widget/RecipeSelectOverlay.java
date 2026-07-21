package com.mastermarisa.maid_restaurant.client.gui.widget;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.uitls.ClientUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class RecipeSelectOverlay extends UIElement {
    private static final int ENTRIES = 8;
    private static final int CATEGORIES = 5;
    private static final Color BG = new Color(32, 32, 32);
    private static final Color COMMON = new Color(48, 48, 48);
    private static final Color SELECTED = new Color(80, 80, 80);
    private static final Color HIGHLIGHT = new Color(64, 64, 64);

    private final Map<RecipeType<?>, List<Recipe<?>>> recipeMap;
    private final List<RecipeType<?>> recipeTypes;
    private final UIRecipeEntry[] uiEntries;
    private final UIRecipeCategory[] uiCategories;
    private final Rectangle categoryFrame;
    private RecipeType<?> selectedType;
    private List<Recipe<?>> currentList;
    private int currentIndex;
    private int categoryIndex;

    public RecipeSelectOverlay(Rectangle frame, Level level,
                               @Nullable Ingredient output, BiConsumer<RecipeType<?>, ResourceLocation> callback) {
        super(frame);
        this.recipeMap = new LinkedHashMap<>();
        if (output != null) {
            if (RecipeCacheBuilder.MATCHED_RECIPE_MAP.containsKey(output)) {
                for (var recipeId : RecipeCacheBuilder.MATCHED_RECIPE_MAP.get(output)) {
                    level.getRecipeManager().byKey(recipeId).ifPresent(recipe -> {
                        if (!this.recipeMap.containsKey(recipe.getType())) {
                            this.recipeMap.put(recipe.getType(), new ArrayList<>());
                        }
                        this.recipeMap.get(recipe.getType()).add(recipe);
                    });
                }
            }
        } else {
            for (var capability : CapabilityRegistry.getAll()) {
                RecipeType<?> type = capability.getRecipeType();
                this.recipeMap.put(type, RecipeCacheBuilder.getAllRecipesFor(level.getRecipeManager(), type));
            }
        }
        this.recipeTypes = CapabilityRegistry.getRegisteredTypes().stream().filter(this.recipeMap::containsKey).toList();
        this.uiEntries = new UIRecipeEntry[ENTRIES];
        for (int i = 0; i < this.uiEntries.length; i++) {
            this.uiEntries[i] = new UIRecipeEntry(new Rectangle(frame.x + 5, frame.y + i * 22 + 4, frame.width - 10, 20), callback, null);
        }
        this.uiCategories = new UIRecipeCategory[CATEGORIES];
        for (int i = 0; i < CATEGORIES; i++) {
            Rectangle categoryFrame = new Rectangle(frame.x - 24, frame.y + i * 22 + 4, 20, 20);
            RecipeType<?> type = i < recipeTypes.size() ? recipeTypes.get(i) : null;
            this.uiCategories[i] = new UIRecipeCategory(categoryFrame, recipeTypes.get(0), type, this::open);
        }
        this.categoryFrame = new Rectangle(frame.x - 26, frame.y + 2, 24, 22 * CATEGORIES + 2);
    }

    public void open(RecipeType<?> type) {
        if (type == this.selectedType) {
            return;
        }
        this.currentIndex = 0;
        this.selectedType = type;
        this.currentList = recipeMap.get(type);
        for (int i = 0; i < ENTRIES; i++) {
            this.uiEntries[i].setRecipe(i < currentList.size() ? currentList.get(i) : null);
        }
        for (int i = 0; i < CATEGORIES; i++) {
            this.uiCategories[i].setSelectedType(type);
        }
    }

    public void open() {
        open(recipeTypes.get(0));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics, mouseX, mouseY);
        graphics.fill(getMinX(), getMinY(), getMaxX(), getMaxY(), BG.getRGB());
        for (var element : uiEntries) {
            element.render(graphics, mouseX, mouseY);
            element.tryRenderTooltip(graphics, mouseX, mouseY);
        }
        for (var category : uiCategories) {
            category.render(graphics, mouseX, mouseY);
            category.tryRenderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return Arrays.stream(uiCategories).anyMatch(r -> r.onMouseClicked(mouseX, mouseY, button))
                || Arrays.stream(uiEntries).anyMatch(r -> r.onMouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (categoryFrame.contains(mouseX, mouseY)) {
            int targetIndex = categoryIndex - Mth.sign(scrollY) * CATEGORIES;
            if (targetIndex >= 0 && targetIndex < recipeTypes.size()) {
                this.categoryIndex = targetIndex;
                for (int i = 0; i < CATEGORIES; i++) {
                    this.uiCategories[i].setRecipeType(categoryIndex + i < recipeTypes.size() ? recipeTypes.get(categoryIndex + i) : null);
                }
                return true;
            }
        }
        if (frame.contains(mouseX, mouseY) && recipeMap.containsKey(selectedType)) {
            int targetIndex = currentIndex - Mth.sign(scrollY) * ENTRIES;
            if (targetIndex >= 0 && targetIndex < currentList.size()) {
                this.currentIndex = targetIndex;
                for (int i = 0; i < ENTRIES; i++) {
                    this.uiEntries[i].setRecipe(currentIndex + i < currentList.size() ? currentList.get(currentIndex + i) : null);
                }
                return true;
            }
        }
        return false;
    }

    public void resize() {
        for (int i = 0; i < this.uiEntries.length; i++) {
            this.uiEntries[i].setMinX(frame.x + 5);
            this.uiEntries[i].setMinY(frame.y + i * 22 + 4);
        }
        for (int i = 0; i < CATEGORIES; i++) {
            this.uiCategories[i].setMinX(frame.x - 24);
            this.uiCategories[i].setMinY(frame.y + i * 22 + 4);
        }
    }

    public void searchBoxContentChanged(String filter) {
        this.currentIndex = 0;
        this.currentList = filter(recipeMap.get(selectedType), filter);
        for (int i = 0; i < ENTRIES; i++) {
            this.uiEntries[i].setRecipe(i < currentList.size() ? currentList.get(i) : null);
        }
    }

    private List<Recipe<?>> filter(List<Recipe<?>> input, String filter) {
        if (mc.level == null) {
            return input;
        }
        return input.stream().filter(recipe -> {
            boolean id = recipe.getId().toString().contains(filter);
            ItemStack result = recipe.getResultItem(mc.level.registryAccess());
            boolean descriptionId = result.getDescriptionId().contains(filter);
            boolean displayName = result.getDisplayName().getString().contains(filter);
            return id || descriptionId || displayName;
        }).toList();
    }

    public static class UIRecipeCategory extends UIElement {
        private final Consumer<RecipeType<?>> callback;
        private RecipeType<?> selectedType;
        @Nullable
        private RecipeType<?> recipeType;

        public UIRecipeCategory(Rectangle frame, RecipeType<?> selectedType, @Nullable RecipeType<?> recipeType, Consumer<RecipeType<?>> callback) {
            super(frame);
            this.selectedType = selectedType;
            this.recipeType = recipeType;
            this.callback = callback;
            if (recipeType != null) {
                ICookCapability capability = CapabilityRegistry.get(recipeType);
                if (capability != null) {
                    this.tooltip.add(Component.translatable("cook_capability.maid_restaurant.%s.name".formatted(capability.getUID())));
                }
            }
        }

        public void setSelectedType(RecipeType<?> selectedType) {
            this.selectedType = selectedType;
        }

        public void setRecipeType(@Nullable RecipeType<?> recipeType) {
            this.recipeType = recipeType;
            this.tooltip.clear();
            if (recipeType != null) {
                ICookCapability capability = CapabilityRegistry.get(recipeType);
                if (capability != null) {
                    this.tooltip.add(Component.translatable("cook_capability.maid_restaurant.%s.name".formatted(capability.getUID())));
                }
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            boolean hovered = frame.contains(mouseX, mouseY);
            int color = recipeType == selectedType ? SELECTED.getRGB() : hovered ? HIGHLIGHT.getRGB() : COMMON.getRGB();
            graphics.fill(frame.x, frame.y, getMaxX(), getMaxY(), color);
            if (recipeType != null && mc.level != null) {
                ICookCapability capability = CapabilityRegistry.get(recipeType);
                if (capability != null) {
                    graphics.renderFakeItem(capability.getIcon(), frame.x + 2, frame.y + 2);
                    graphics.renderItemDecorations(font, capability.getIcon(), frame.x + 2, frame.y + 2);
                }
            }
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (recipeType != null && button == 0 && frame.contains(mouseX, mouseY)) {
                callback.accept(recipeType);
                return true;
            }
            return false;
        }
    }

    public static class UIRecipeEntry extends UIElement {
        private final BiConsumer<RecipeType<?>, ResourceLocation> callback;
        @Nullable
        private Recipe<?> recipe;

        public UIRecipeEntry(Rectangle frame, BiConsumer<RecipeType<?>, ResourceLocation> callback, @Nullable Recipe<?> recipe) {
            super(frame);
            this.callback = callback;
            this.recipe = recipe;
        }

        public void setRecipe(@Nullable Recipe<?> recipe) {
            this.recipe = recipe;
            this.tooltip.clear();
            if (recipe != null) {
                this.tooltip.add(Component.literal(recipe.getId().toString()).withStyle(ChatFormatting.GRAY));
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            if (recipe != null && mc.level != null) {
                int color = frame.contains(mouseX, mouseY) ? HIGHLIGHT.getRGB() : COMMON.getRGB();
                graphics.fill(getMinX(), getMinY(), getMaxX(), getMaxY(), color);
                graphics.renderFakeItem(recipe.getResultItem(mc.level.registryAccess()), getMinX() + 3, getMinY() + 2);
                List<IngredientStack> ingredients = RecipeCacheBuilder.getIngredientStacks(recipe.getId());
                for (int i = 0; i < ingredients.size(); i++) {
                    int x = getMinX() + i * 22 + 42;
                    int y = getMinY() + 2;
                    ClientUtil.renderIngredientStack(graphics, x, y, ingredients.get(i), mc.level.getGameTime(), 20);
                }
            }
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            if (recipe != null && button == 0 && frame.contains(mouseX, mouseY)) {
                callback.accept(recipe.getType(), recipe.getId());
                return true;
            }
            return false;
        }
    }
}
