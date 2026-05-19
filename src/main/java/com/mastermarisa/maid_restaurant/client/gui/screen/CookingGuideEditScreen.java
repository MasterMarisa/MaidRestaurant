package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.client.gui.screen.widgets.RecipeTreeWidget;
import com.mastermarisa.maid_restaurant.core.recipe.RecipeNode;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class CookingGuideEditScreen extends Screen {
    private static final Minecraft minecraft;
    private static final Font font;

    private static final int SCREEN_WIDTH = 280;
    private static final int SCREEN_HEIGHT = 210;
    private static final int TREE_X = 10;
    private static final int TREE_Y = 15;
    private static final int TREE_WIDTH = 140;
    private static final int TREE_HEIGHT = 155;

    private final ItemStack guideItem;
    private final InteractionHand hand;
    private RecipeNode rootNode;
    @Nullable
    private RecipeNode selectedNode;
    private RecipeTreeWidget treeWidget;
    private boolean dirty;

    public CookingGuideEditScreen(ItemStack guideItem, InteractionHand hand) {
        super(Component.empty());
        this.guideItem = guideItem;
        this.hand = hand;
        this.dirty = false;
    }

    public static void open(ItemStack guideItem, InteractionHand hand) {
        minecraft.setScreen(new CookingGuideEditScreen(guideItem, hand));
    }

    @Override
    protected void init() {
        super.init();
        rootNode = loadFromGuide(guideItem);
        int guiLeft = (this.width - SCREEN_WIDTH) / 2;
        int guiTop = (this.height - SCREEN_HEIGHT) / 2;

        treeWidget = new RecipeTreeWidget(new Rectangle(guiLeft + TREE_X, guiTop + TREE_Y, TREE_WIDTH, TREE_HEIGHT));
        treeWidget.build(rootNode);

        if (selectedNode == null) {
            selectedNode = rootNode;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        treeWidget.render(graphics, mouseX, mouseY, selectedNode);
    }

    private RecipeNode loadFromGuide(ItemStack guideItem) {
        CompoundTag tag = CookingGuideItem.getRecipeRoot(guideItem);
        return RecipeNode.fromNBT(tag);
    }

    static {
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }
}
