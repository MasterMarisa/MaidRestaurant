package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.client.gui.widget.RecipeSelectOverlay;
import com.mastermarisa.maid_restaurant.core.capability.CapabilityRegistry;
import com.mastermarisa.maid_restaurant.core.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeCacheBuilder;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.SaveRecipeTreeMessage;
import com.mastermarisa.maid_restaurant.uitls.ClientUtils;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CookingGuideScreen extends Screen {
    private static final Minecraft minecraft;
    private static final Font font;
    private static final int ROW_HEIGHT = 22;
    private static final int INDENT_WIDTH = 36;
    private static final Color BG = new Color(24, 24, 24);
    private static final Color COMMON = new Color(48, 48, 48);
    private static final Color HIGHLIGHT = new Color(64, 64, 64);

    private final List<NodeEntry> entries;
    private final List<ButtonEntry> buttons;
    private final EditBox searchBox;
    private Vec2 offset;
    private RecipeNode root;
    @Nullable
    private RecipeNode selectedNode;
    @Nullable
    private RecipeSelectOverlay selectOverlay;

    public CookingGuideScreen(ItemStack itemStack) {
        super(Component.empty());
        this.entries = new ArrayList<>();
        this.buttons = new ArrayList<>();
        this.searchBox = new EditBox(font, ClientUtils.getScreenCenterX() - 109, ClientUtils.getScreenCenterY() - 111, 90, 16, Component.empty());
        this.searchBox.setMaxLength(50);
        this.searchBox.setEditable(true);
        CompoundTag tag = CookingGuideItem.getRecipeRoot(itemStack);
        this.root = tag.isEmpty() ? new RecipeNode() : RecipeNode.fromNBT(tag);
        rebindIngredient(this.root);
        this.searchBox.setResponder(this::onSearchBoxContentChanged);
        this.offset = Vec2.ZERO;
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new CookingGuideScreen(stack));
    }

    @Override
    protected void init() {
        super.init();
        this.entries.clear();
        flattenTree(this.root, 0, null);
        this.buttons.clear();
        if (this.entries.size() <= 1) {
            this.buttons.add(new ButtonEntry(new Rectangle(18, 26, 7, 7), -1, 1));
        } else {
            for (int i = 0; i < this.entries.size(); i++) {
                NodeEntry entry = this.entries.get(i);
                int x = 30 + entry.depth * INDENT_WIDTH;
                int y = 20 + i * ROW_HEIGHT;
                if (!entry.node.isLeaf()) {
                    this.buttons.add(new ButtonEntry(new Rectangle(x - 12, y + ROW_HEIGHT / 2 - 5, 7, 7), i, 0));
                }
                if (entry.canExpand() && entry.node.isLeaf()) {
                    this.buttons.add(new ButtonEntry(new Rectangle(x + 39, y + ROW_HEIGHT / 2 - 5, 7, 7), i, 1));
                }
            }
        }
        if (this.selectOverlay != null) {
            this.removeWidget(this.searchBox);
            this.addRenderableWidget(this.searchBox);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        var window = minecraft.getWindow();
        graphics.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), BG.getRGB());
        PoseStack pose = graphics.pose();
        pose.pushPose();
        {
            pose.translate(offset.x, offset.y, 0);
            if (this.entries.size() <= 1) {
                graphics.drawCenteredString(font, "+", 22, 26, Color.WHITE.getRGB());
            } else {
                for (int i = 0; i < this.entries.size(); i++) {
                    renderNode(graphics, i, this.entries.get(i), mouseX, mouseY);
                }
            }
        }
        pose.popPose();
        if (selectOverlay != null) {
            pose.pushPose();
            {
                pose.translate(0, 0, 300);
                searchBox.render(graphics, mouseX, mouseY, partialTick);
                selectOverlay.render(graphics, mouseX, mouseY);
            }
            pose.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (selectOverlay != null && selectOverlay.onMouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        }
        if (pButton == 0) {
            for (var button : this.buttons) {
                if (button.frame.contains(pMouseX - offset.x, pMouseY - offset.y)) {
                    if (button.button == 0) {
                        NodeEntry entry = this.entries.get(button.index);
                        entry.node.getChildren().clear();
                        entry.node.setStep(null);
                        if (button.index == 0) {
                            this.root = new RecipeNode();
                        }
                        save();
                        init();
                    } else if (button.button == 1) {
                        Ingredient ingredient = null;
                        if (button.index != -1) {
                            NodeEntry entry = this.entries.get(button.index);
                            ingredient = entry.node.getIngredient();
                            this.selectedNode = entry.node;
                        }
                        if (minecraft.level != null) {
                            openSelectOverlay(minecraft.level, ingredient);
                        }
                    }
                    return true;
                }
            }
        }
        if (selectOverlay != null && !this.searchBox.isHovered()) {
            this.selectOverlay = null;
            this.selectedNode = null;
            this.removeWidget(this.searchBox);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (selectOverlay != null && selectOverlay.onMouseScrolled(pMouseX, pMouseY, 0, pDelta)) {
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton == 1) {
            float x = Math.min(0, (float) (offset.x + pDragX * 0.6));
            float y = Math.min(0, (float) (offset.y + pDragY * 0.6));
            this.offset = new Vec2(x, y);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String text = this.searchBox.getValue();
        super.resize(minecraft, width, height);
        this.searchBox.setValue(text);
        this.searchBox.setX(ClientUtils.getScreenCenterX() - 109);
        this.searchBox.setY(ClientUtils.getScreenCenterY() - 111);
        if (this.selectOverlay != null) {
            this.selectOverlay.setCenter(ClientUtils.getScreenCenterX(), ClientUtils.getScreenCenterY());
            this.selectOverlay.resize();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void save() {
        NetworkHandler.sendToServer(new SaveRecipeTreeMessage(this.root.serializeNBT()));
    }

    private void onSearchBoxContentChanged(String filter) {
        if (this.selectOverlay != null) {
            this.selectOverlay.searchBoxContentChanged(filter);
        }
    }

    private void openSelectOverlay(Level level, @Nullable Ingredient output) {
        if (this.selectOverlay != null) {
            return;
        }
        Rectangle frame = new Rectangle(ClientUtils.getScreenCenterX() - 110,
                ClientUtils.getScreenCenterY() - 91, 220, 182);
        this.selectOverlay = new RecipeSelectOverlay(frame, level, output, this::onRecipeSelected);
        this.selectOverlay.open();
        this.addRenderableWidget(this.searchBox);
    }

    private void onRecipeSelected(RecipeType<?> type, ResourceLocation recipeId) {
        this.selectOverlay = null;
        ICookCapability capability = CapabilityRegistry.get(type);
        if (capability != null) {
            if (this.selectedNode == null) {
                this.root = fromRecipe(capability.getUID(), recipeId);
            } else {
                this.selectedNode.setStep(new RecipeStep(capability.getUID(), recipeId));
                buildChildren(this.selectedNode);
            }
            save();
            init();
        }
        this.selectedNode = null;
        this.removeWidget(this.searchBox);
    }

    /**
     * 重新绑定根节点及其子树绑定的Ingredient,用于后续在缓存中检索配方
     * @param node 根节点
     */
    private void rebindIngredient(RecipeNode node) {
        RecipeStep step = node.getStep();
        if (step == null || step.recipeId() == null) {
            return;
        }

        List<IngredientStack> ingredients = RecipeCacheBuilder.getIngredientStacks(step.recipeId());
        for (var child : node.getChildren()) {
            for (var stack : ingredients) {
                if (ItemUtils.equals(child.getIngredient(), stack.getIngredient())) {
                    child.setIngredient(stack.getIngredient());
                    child.setCount(child.getCount());
                    break;
                }
            }
            rebindIngredient(child);
        }
    }

    /**
     * 将树展开为便于渲染的节点列表
     * @param node 展开的节点
     * @param depth 节点深度
     * @param parent 父节点
     */
    private void flattenTree(RecipeNode node, int depth, @Nullable RecipeNode parent) {
        boolean canExpand = RecipeCacheBuilder.MATCHED_RECIPE_MAP.containsKey(node.getIngredient());
        this.entries.add(NodeEntry.of(node, depth, parent, canExpand));
        for (var child : node.getChildren()) {
            flattenTree(child, depth + 1, node);
        }
    }

    /**
     * 用于从配方生成根节点
     * @param capabilityUID capabilityUID
     * @param recipeId 配方Id
     * @return 生成的节点
     */
    private RecipeNode fromRecipe(String capabilityUID, ResourceLocation recipeId) {
        RecipeNode node = new RecipeNode();
        Level level = minecraft.level;
        if (level == null) {
            return node;
        }

        level.getRecipeManager().byKey(recipeId).ifPresent(recipe -> {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            node.setIngredient(Ingredient.of(result));
            node.setCount(result.getCount());
            node.setStep(new RecipeStep(capabilityUID, recipeId));
            buildChildren(node);
        });
        return node;
    }

    private void buildChildren(RecipeNode node) {
        RecipeStep step = node.getStep();
        if (step == null || step.recipeId() == null) {
            return;
        }

        Level level = minecraft.level;
        if (level == null) {
            return;
        }

        Recipe<?> recipe = node.getRecipe(level.getRecipeManager());
        ICookCapability capability = node.getCapability();
        if (recipe == null || capability == null) {
            return;
        }

        for (var stack : RecipeCacheBuilder.getIngredientStacks(step.recipeId())) {
            int count = capability.getIngredientCount(level, recipe, node.getCount(), stack);
            RecipeNode child = new RecipeNode(stack.getIngredient(), count, null);
            node.addChild(child);
        }
    }

    private void renderNode(GuiGraphics graphics, int row, NodeEntry entry, int mouseX, int mouseY) {
        int x = 30 + entry.depth * INDENT_WIDTH;
        int y = 20 + row * ROW_HEIGHT;
        RecipeNode node = entry.node;
        ItemStack icon = Items.CHEST.getDefaultInstance();
        ICookCapability capability = node.getCapability();
        if (capability != null) {
            icon = capability.getIcon();
        }
        long gameTime = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        graphics.renderFakeItem(icon, x, y);
        ClientUtils.renderIngredientStack(graphics, x + 18, y, new IngredientStack(node.getIngredient(), node.getCount()), gameTime, 20);
        if (!entry.node.isLeaf()) {
            graphics.drawCenteredString(font, "-", x - 8, y + ROW_HEIGHT / 2 - 5, Color.WHITE.getRGB());
        }
        if (entry.canExpand && entry.node.isLeaf()) {
            graphics.drawCenteredString(font, "+", x + 43, y + ROW_HEIGHT / 2 - 5, Color.WHITE.getRGB());
        }
    }

    public record NodeEntry(RecipeNode node, int depth, @Nullable RecipeNode parent, boolean canExpand) {
        public static NodeEntry of(RecipeNode node, int depth, @Nullable RecipeNode parent, boolean canExpand) {
            return new NodeEntry(node, depth, parent, canExpand);
        }
    }

    public record ButtonEntry(Rectangle frame, int index, int button) {}

    static {
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }
}