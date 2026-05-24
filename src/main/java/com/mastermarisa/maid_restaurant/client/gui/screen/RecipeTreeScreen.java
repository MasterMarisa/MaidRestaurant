package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.SaveRecipeTreeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class RecipeTreeScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int INDENT_WIDTH = 16;
    private static final int LIST_TOP = 40;
    private static final int LIST_BOTTOM_OFFSET = 30;
    private static final int BTN_WIDTH = 14;
    private static final int BTN_HEIGHT = 14;

    private final ItemStack itemStack;
    private RecipeNode root;
    private final List<TreeNode> flatNodes = new ArrayList<>();
    private int scrollOffset;
    private RecipeSelectOverlay overlay;
    private long tick;
    private int iconCycle;

    private int listLeft;
    private int listWidth;

    private record TreeNode(RecipeNode node, int depth, int parentFlatIndex, int selfIndexInParent, boolean canExpand) {}

    public RecipeTreeScreen(ItemStack itemStack) {
        super(Component.empty());
        this.itemStack = itemStack;
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new RecipeTreeScreen(stack));
    }

    @Override
    protected void init() {
        super.init();
        this.listLeft = this.width / 2 - 150;
        this.listWidth = 300;
        loadRoot();
        rebuildFlatList();
    }

    private void loadRoot() {
        var tag = CookingGuideItem.getRecipeRoot(itemStack);
        if (tag.isEmpty()) {
            root = new RecipeNode();
        } else {
            root = RecipeNode.fromNBT(tag);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tick++;
        if (tick % 20 == 0) {
            iconCycle++;
        }
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTreeList(graphics, mouseX, mouseY);
        renderButtons(graphics, mouseX, mouseY);
        if (overlay != null) {
            overlay.render(graphics, mouseX, mouseY);
        }
    }

    private void renderTreeList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listBottom = this.height - LIST_BOTTOM_OFFSET;
        int startY = LIST_TOP - scrollOffset;

        for (int i = 0; i < flatNodes.size(); i++) {
            int y = startY + i * ROW_HEIGHT;
            if (y + ROW_HEIGHT < LIST_TOP || y > listBottom) continue;
            TreeNode tn = flatNodes.get(i);
            int x = listLeft + tn.depth * INDENT_WIDTH;

            int itemX = x + 2;
            int itemY = y + 3;
            ItemStack[] items = tn.node.getOutput().getItems();
            if (items.length > 0) {
                int idx = iconCycle % items.length;
                graphics.renderFakeItem(items[idx], itemX, itemY);
            }

            String label = items.length > 0 ? items[iconCycle % items.length].getHoverName().getString() : "?";
            if (tn.node.hasCombineStep()) {
                ResourceLocation rid = tn.node.getCombineStep().getRecipeId();
                if (rid != null) label = label + " (" + rid.getPath() + ")";
            }
            graphics.drawString(this.font, label + " x" + tn.node.getOutputCount(), itemX + 20, y + 7, 0xFFFFFF);

            int btnRight = listLeft + listWidth - BTN_WIDTH - 4;
            int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;

            if (tn.canExpand) {
                int plusX = btnRight - BTN_WIDTH - 4;
                renderButton(graphics, plusX, btnY, BTN_WIDTH, BTN_HEIGHT, "+", mouseX, mouseY);
            }

            renderButton(graphics, btnRight, btnY, BTN_WIDTH, BTN_HEIGHT, "x", mouseX, mouseY);
        }
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int w, int h, String text, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hovered ? 0x80FFFFFF : 0x40FFFFFF;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.drawCenteredString(this.font, text, x + w / 2, y + (h - 8) / 2, 0xFFFFFF);
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        int cx = listLeft + listWidth / 2;
        int addY = LIST_TOP - 22;
        renderButton(graphics, cx - 50, addY, 100, 18, "+ New Root", mouseX, mouseY);

        int btnY = this.height - 24;
        int saveX = this.width / 2 - 55;
        int closeX = this.width / 2 + 5;
        renderButton(graphics, saveX, btnY, 50, 18, "Save", mouseX, mouseY);
        renderButton(graphics, closeX, btnY, 50, 18, "Close", mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (overlay != null) {
            if (overlay.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (!overlay.isInside(mouseX, mouseY)) {
                overlay = null;
                return true;
            }
            return true;
        }

        int listBottom = this.height - LIST_BOTTOM_OFFSET;
        int startY = LIST_TOP - scrollOffset;

        for (int i = 0; i < flatNodes.size(); i++) {
            int y = startY + i * ROW_HEIGHT;
            if (y + ROW_HEIGHT < LIST_TOP || y > listBottom) continue;
            TreeNode tn = flatNodes.get(i);

            int btnRight = listLeft + listWidth - BTN_WIDTH - 4;
            int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;

            if (tn.canExpand) {
                int plusX = btnRight - BTN_WIDTH - 4;
                if (mouseX >= plusX && mouseX <= plusX + BTN_WIDTH && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT) {
                    openOverlay((recipeId, capabilityUID) -> {
                        RecipeNode newNode = createNodeFromRecipe(recipeId, capabilityUID);
                        tn.node.addChild(newNode);
                        rebuildFlatList();
                    });
                    return true;
                }
            }

            if (mouseX >= btnRight && mouseX <= btnRight + BTN_WIDTH && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT) {
                deleteNode(tn);
                rebuildFlatList();
                return true;
            }

            int rowX = listLeft + tn.depth * INDENT_WIDTH;
            if (mouseX >= rowX && mouseX <= listLeft + listWidth && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                openOverlay((recipeId, capabilityUID) -> {
                    tn.node.setOutput(createOutputFromRecipe(recipeId), tn.node.getOutputCount());
                    tn.node.setCombineStep(new RecipeStep(capabilityUID, recipeId));
                    tn.node.setChildren(new ArrayList<>());
                    rebuildChildren(tn.node);
                    rebuildFlatList();
                });
                return true;
            }
        }

        if (button == 0) {
            int addY = LIST_TOP - 22;
            int cx = listLeft + listWidth / 2;
            if (mouseX >= cx - 50 && mouseX <= cx + 50 && mouseY >= addY && mouseY <= addY + 18) {
                openOverlay((recipeId, capabilityUID) -> {
                    root = createNodeFromRecipe(recipeId, capabilityUID);
                    rebuildFlatList();
                });
                return true;
            }

            int btnY = this.height - 24;
            int saveX = this.width / 2 - 55;
            int closeX = this.width / 2 + 5;
            if (mouseX >= saveX && mouseX <= saveX + 50 && mouseY >= btnY && mouseY <= btnY + 18) {
                NetworkHandler.sendToServer(new SaveRecipeTreeMessage(root.serializeNBT()));
                return true;
            }
            if (mouseX >= closeX && mouseX <= closeX + 50 && mouseY >= btnY && mouseY <= btnY + 18) {
                onClose();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (overlay != null) {
            overlay.mouseScrolled(mouseX, mouseY, delta);
            return true;
        }
        scrollOffset = Math.max(0, scrollOffset - (int) delta * 12);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (overlay != null && overlay.searchFocused) {
            if (keyCode == 256) {
                overlay = null;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (overlay != null && overlay.searchFocused) {
            return super.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openOverlay(BiConsumer<ResourceLocation, String> onSelect) {
        this.overlay = new RecipeSelectOverlay(onSelect);
        this.overlay.init(this.width, this.height);
    }

    private void deleteNode(TreeNode tn) {
        if (tn.parentFlatIndex < 0) {
            root = new RecipeNode();
            return;
        }
        TreeNode parent = flatNodes.get(tn.parentFlatIndex);
        parent.node.getChildren().remove(tn.selfIndexInParent);
    }

    private RecipeNode createNodeFromRecipe(ResourceLocation recipeId, String capabilityUID) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return new RecipeNode();
        var opt = mc.level.getRecipeManager().byKey(recipeId);
        if (opt.isEmpty()) return new RecipeNode();

        Recipe<?> recipe = opt.get();
        RegistryAccess registryAccess = mc.level.registryAccess();
        ItemStack result = recipe.getResultItem(registryAccess);
        int count = result.getCount();
        Ingredient output = Ingredient.of(result);

        RecipeNode node = new RecipeNode(output, count, new RecipeStep(capabilityUID, recipeId));
        rebuildChildren(node);
        return node;
    }

    private Ingredient createOutputFromRecipe(ResourceLocation recipeId) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return Ingredient.EMPTY;
        var opt = mc.level.getRecipeManager().byKey(recipeId);
        if (opt.isEmpty()) return Ingredient.EMPTY;
        ItemStack result = opt.get().getResultItem(mc.level.registryAccess());
        return Ingredient.of(result);
    }

    private void rebuildChildren(RecipeNode node) {
        if (node.getCombineStep() == null || node.getCombineStep().getRecipeId() == null) return;
        ResourceLocation recipeId = node.getCombineStep().getRecipeId();
        List<IngredientStack> stacks = RecipeCacheBuilder.getIngredientStacks(recipeId);
        for (IngredientStack stack : stacks) {
            Ingredient ingredient = stack.getIngredient();
            RecipeNode child = new RecipeNode(ingredient, stack.getCount(), null);
            node.addChild(child);
        }
    }

    private void rebuildFlatList() {
        flatNodes.clear();
        buildFlatList(root, 0, -1, 0);
    }

    private void buildFlatList(RecipeNode node, int depth, int parentIndex, int selfIndexInParent) {
        boolean canExpand = hasMatchingRecipes(node.getOutput());
        int myIndex = flatNodes.size();
        flatNodes.add(new TreeNode(node, depth, parentIndex, selfIndexInParent, canExpand));
        for (int i = 0; i < node.getChildren().size(); i++) {
            buildFlatList(node.getChildren().get(i), depth + 1, myIndex, i);
        }
    }

    private boolean hasMatchingRecipes(Ingredient ingredient) {
        return RecipeCacheBuilder.MATCHED_RECIPE_MAP.containsKey(ingredient);
    }

    private class RecipeSelectOverlay {
        private static final int OVERLAY_W = 340;
        private static final int OVERLAY_H = 260;
        private static final int ROW_H = 20;

        private final BiConsumer<ResourceLocation, String> onSelect;
        private int oLeft, oTop;
        private int listScroll;
        private String searchText = "";
        private int selectedTab;
        private final List<ICookCapability> capabilities;
        private boolean searchFocused;

        RecipeSelectOverlay(BiConsumer<ResourceLocation, String> onSelect) {
            this.onSelect = onSelect;
            this.capabilities = new ArrayList<>(CapabilityRegistry.getAll());
            this.capabilities.sort(Comparator.comparing(ICookCapability::getUID));
        }

        void init(int screenWidth, int screenHeight) {
            this.oLeft = (screenWidth - OVERLAY_W) / 2;
            this.oTop = (screenHeight - OVERLAY_H) / 2;
        }

        boolean isInside(double mx, double my) {
            return mx >= oLeft && mx <= oLeft + OVERLAY_W && my >= oTop && my <= oTop + OVERLAY_H;
        }

        void render(GuiGraphics graphics, int mouseX, int mouseY) {
            graphics.fill(0, 0, RecipeTreeScreen.this.width, RecipeTreeScreen.this.height, 0x80000000);
            graphics.fill(oLeft, oTop, oLeft + OVERLAY_W, oTop + OVERLAY_H, 0xFF333333);
            graphics.drawString(RecipeTreeScreen.this.font, "Select Recipe", oLeft + 6, oTop + 6, 0xFFFFFF);

            int searchY = oTop + 22;
            String displayText = searchText.isEmpty() && !searchFocused ? "Search..." : searchText;
            int textColor = searchText.isEmpty() && !searchFocused ? 0xFF888888 : 0xFFFFFFFF;
            graphics.fill(oLeft + 6, searchY, oLeft + OVERLAY_W - 6, searchY + 14, 0xFF000000);
            graphics.drawString(RecipeTreeScreen.this.font, displayText, oLeft + 8, searchY + 3, textColor);

            int tabY = oTop + 40;
            for (int i = 0; i < capabilities.size(); i++) {
                int tx = oLeft + 6 + i * 80;
                int bg = (i == selectedTab) ? 0xFF555555 : 0xFF333333;
                graphics.fill(tx, tabY, tx + 76, tabY + 16, bg);
                var rtype = capabilities.get(i).getRecipeType();
                String rn = rtype != null ? rtype.toString().replace("minecraft:", "") : capabilities.get(i).getUID();
                graphics.drawCenteredString(RecipeTreeScreen.this.font, rn, tx + 38, tabY + 4, 0xFFFFFF);
            }

            int listY = tabY + 20;
            List<ResourceLocation> recipes = new ArrayList<>();
            var mc = Minecraft.getInstance();
            if (mc.level != null && !capabilities.isEmpty()) {
                var cap = capabilities.get(selectedTab);
                for (Recipe<?> r : RecipeCacheBuilder.getAllRecipesFor(mc.level.getRecipeManager(), cap.getRecipeType())) {
                    String name = r.getResultItem(mc.level.registryAccess()).getHoverName().getString().toLowerCase();
                    String rId = r.getId().toString().toLowerCase();
                    String q = searchText.toLowerCase();
                    if (q.isEmpty() || name.contains(q) || rId.contains(q)) {
                        recipes.add(r.getId());
                    }
                }
            }

            int visibleRows = (OVERLAY_H - (tabY + 20) - oTop - 4) / ROW_H;
            listScroll = Math.max(0, Math.min(listScroll, Math.max(0, recipes.size() - visibleRows)));

            for (int i = listScroll; i < Math.min(recipes.size(), listScroll + visibleRows); i++) {
                ResourceLocation rid = recipes.get(i);
                int ry = listY + (i - listScroll) * ROW_H;
                boolean hovered = mouseX >= oLeft + 8 && mouseX <= oLeft + OVERLAY_W - 8 && mouseY >= ry && mouseY < ry + ROW_H;
                if (hovered) {
                    graphics.fill(oLeft + 6, ry, oLeft + OVERLAY_W - 6, ry + ROW_H, 0x60FFFFFF);
                }

                if (mc.level != null) {
                    var opt = mc.level.getRecipeManager().byKey(rid);
                    if (opt.isPresent()) {
                        ItemStack icon = opt.get().getResultItem(mc.level.registryAccess());
                        graphics.renderFakeItem(icon, oLeft + 10, ry + 2);
                        graphics.drawString(RecipeTreeScreen.this.font, icon.getHoverName().getString(), oLeft + 30, ry + 5, 0xFFFFFF);
                        graphics.drawString(RecipeTreeScreen.this.font, rid.toString(), oLeft + 180, ry + 5, 0xFFAAAAAA);
                    }
                }
            }
        }

        boolean mouseClicked(double mx, double my, int button) {
            if (!isInside(mx, my)) return false;

            int searchY = oTop + 22;
            int listY = oTop + 40 + 20;

            if (mx >= oLeft + 6 && mx <= oLeft + OVERLAY_W - 6 && my >= searchY && my <= searchY + 14) {
                searchFocused = true;
                return true;
            }

            int tabY = oTop + 40;
            for (int i = 0; i < capabilities.size(); i++) {
                int tx = oLeft + 6 + i * 80;
                if (mx >= tx && mx <= tx + 76 && my >= tabY && my <= tabY + 16) {
                    selectedTab = i;
                    listScroll = 0;
                    searchText = "";
                    return true;
                }
            }

            List<ResourceLocation> recipes = getFilteredRecipes();
            int visibleRows = (OVERLAY_H - (oTop + 40 + 20) - oTop - 4) / ROW_H;
            for (int i = listScroll; i < Math.min(recipes.size(), listScroll + visibleRows); i++) {
                int ry = listY + (i - listScroll) * ROW_H;
                if (mx >= oLeft + 8 && mx <= oLeft + OVERLAY_W - 8 && my >= ry && my < ry + ROW_H) {
                    ResourceLocation rid = recipes.get(i);
                    String uid = capabilities.get(selectedTab).getUID();
                    onSelect.accept(rid, uid);
                    RecipeTreeScreen.this.overlay = null;
                    return true;
                }
            }

            return true;
        }

        void mouseScrolled(double mx, double my, double delta) {
            listScroll = Math.max(0, listScroll - (int) delta * 2);
        }

        private List<ResourceLocation> getFilteredRecipes() {
            List<ResourceLocation> recipes = new ArrayList<>();
            var mc = Minecraft.getInstance();
            if (mc.level == null || capabilities.isEmpty()) return recipes;
            var cap = capabilities.get(selectedTab);
            for (Recipe<?> r : RecipeCacheBuilder.getAllRecipesFor(mc.level.getRecipeManager(), cap.getRecipeType())) {
                String name = r.getResultItem(mc.level.registryAccess()).getHoverName().getString().toLowerCase();
                String rId = r.getId().toString().toLowerCase();
                String q = searchText.toLowerCase();
                if (q.isEmpty() || name.contains(q) || rId.contains(q)) {
                    recipes.add(r.getId());
                }
            }
            return recipes;
        }
    }
}
