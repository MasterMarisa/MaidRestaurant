package com.mastermarisa.maid_restaurant.client.gui.screen.widgets;

import com.mastermarisa.maid_restaurant.core.recipe.RecipeNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeTreeWidget extends UIElement {
    private static final int INDENT = 20;
    private static final int ROW_HEIGHT = 20;
    private static final ItemStack CHEST = Items.CHEST.getDefaultInstance();
    private static final ItemStack CRAFTING_TABLE = Items.CRAFTING_TABLE.getDefaultInstance();
    private static final ItemStack BARREL = Items.BARREL.getDefaultInstance();

    private final List<TreeNodeEntry> entries = new ArrayList<>();
    private int scrollOffset = 0;

    public RecipeTreeWidget(Rectangle frame) {
        super(frame);
    }

    public void build(RecipeNode root) {
        entries.clear();
        flattenTree(root, 0, null);
    }

    private void flattenTree(RecipeNode node, int depth, @Nullable RecipeNode parent) {
        entries.add(new TreeNodeEntry(node, depth, parent));
        for (RecipeNode child : node.getChildren()) {
            flattenTree(child, depth + 1, node);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, @Nullable RecipeNode selectedNode) {
        int visibleRows = getHeight() / ROW_HEIGHT;

        for (int i = 0; i < entries.size(); i++) {
            int row = i - scrollOffset;
            if (row < 0 || row >= visibleRows) {
                continue;
            }

            TreeNodeEntry entry = entries.get(i);
            RecipeNode node = entry.node;
            int indentX = getMinX() + entry.depth * INDENT;
            int rowY = getMinY() + row * ROW_HEIGHT;

            graphics.renderFakeItem(getIcon(node), indentX, rowY);
            graphics.drawString(font, "x" + node, indentX + 18, rowY + 5, Color.WHITE.getRGB());
        }
    }

    public void scroll(int amount) {
        scrollOffset += amount;
        int maxScroll = Math.max(0, entries.size() - getHeight() / ROW_HEIGHT);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
    }

    @Nullable
    public RecipeNode getNodeAt(double mouseX, double mouseY) {
        if (!frame.contains(mouseX, mouseY)) {
            return null;
        }
        int row = (int) (mouseY - getMinY()) / ROW_HEIGHT + scrollOffset;
        if (row >= 0 && row < entries.size()) {
            return entries.get(row).node;
        }
        return null;
    }

    @Nullable
    public RecipeNode getParentOf(RecipeNode node) {
        for (TreeNodeEntry entry : entries) {
            if (entry.node == node) {
                return entry.parent;
            }
        }
        return null;
    }

    private static ItemStack getIcon(RecipeNode node) {
        Ingredient output = node.getOutput();
        if (output.isEmpty()) {
            return BARREL;
        }
        ItemStack[] items = output.getItems();
        if (items.length > 0 && !items[0].isEmpty()) {
            return items[0];
        }
        return BARREL;
    }

    public record TreeNodeEntry(RecipeNode node, int depth, @Nullable RecipeNode parent) {}
}
