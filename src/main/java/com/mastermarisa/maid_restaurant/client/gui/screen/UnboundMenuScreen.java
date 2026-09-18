package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class UnboundMenuScreen extends Screen {
    private static final Minecraft minecraft;
    private static final Font font;
    private static final ResourceLocation bgImg = MaidRestaurant.modLoc("textures/gui/unbound_menu.png");
    private static final ResourceLocation invImg = MaidRestaurant.modLoc("textures/gui/clipboard.png");
    private static final Color lessBlack = new Color(0, 0, 0, 32);
    private static final Color leastBlack = new Color(0, 0, 0, 16);

    private final ItemStack itemStack;
    private final Player player;
    private final List<RecipeInfo> writtenEntries;
    private final List<RecipeInfo> availableEntries;
    private int currentIndex;

    public UnboundMenuScreen(ItemStack itemStack, Player player) {
        super(Component.empty());
        this.itemStack = itemStack;
        this.player = player;
        this.writtenEntries = new ArrayList<>();
        this.availableEntries = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.COOKING_GUIDE.get())) {
                CompoundTag tag = CookingGuideItem.getRecipeRoot(stack);
                RecipeNode root = tag.isEmpty() ? new RecipeNode() : RecipeNode.fromNBT(tag);
                this.availableEntries.add(RecipeInfo.fromNode(root));
            }
        }
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();
    }

    public static void open(ItemStack itemStack, Player player) {
        Minecraft.getInstance().setScreen(new UnboundMenuScreen(itemStack, player));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderBackground(graphics);
        int x = getScreenCenterX() - 98;
        int y = getScreenCenterY() - 108;
        // 210 x 216
        graphics.blit(bgImg, x, y, 0, 0, 210, 216, 360, 358);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static int getScreenCenterX(){
        return minecraft.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return minecraft.getWindow().getGuiScaledHeight() / 2;
    }

    private record MenuEntry(RecipeNode root, Component name) {}

    private record ButtonEntry(Rectangle frame, int index, int button) {}

    private static class RecipeInfo {
        private RecipeNode root;
        private IngredientStack output;
        private List<IngredientStack> inputs;
        private List<ItemStack> workBlocks;

        public static RecipeInfo fromNode(RecipeNode root) {
            RecipeInfo info = new RecipeInfo();
            info.root = root;
            info.output = root.getOutputAsStack();
            List<RecipeStep> steps = new ArrayList<>();
            traversalStep(root, steps);
            List<ItemStack> workBlocks = new ArrayList<>();
            for (var step : steps) {
                ICookCapability capability = step.getCapability();
                if (capability != null) {
                    ItemStack icon = capability.getIcon();
                    boolean contained = false;
                    for (var stack : workBlocks) {
                        if (ItemStack.isSameItem(icon, stack)) {
                            contained = true;
                            break;
                        }
                    }
                    if (!contained) {
                        workBlocks.add(capability.getIcon());
                    }
                }
            }
            info.workBlocks = workBlocks;
            List<RecipeNode> leaves = new ArrayList<>();
            traversalLeaf(root, leaves);
            List<IngredientStack> inputs = new ArrayList<>();
            for (RecipeNode node : leaves) {
                IngredientStack input = node.getOutputAsStack();
                boolean merged = false;
                for (IngredientStack stack : inputs) {
                    if (IngredientUtil.equals(input.getIngredient(), stack.getIngredient())) {
                        merged = true;
                        stack.setCount(stack.getCount() + input.getCount());
                        break;
                    }
                }
                if (!merged) {
                    inputs.add(input);
                }
            }
            info.inputs = inputs;
            return info;
        }

        private static void traversalStep(RecipeNode node, List<RecipeStep> steps) {
            if (node.isLeaf()) {
                return;
            }
            steps.add(node.getStep());
            for (RecipeNode child : node.getChildren()) {
                traversalStep(child, steps);
            }
        }

        private static void traversalLeaf(RecipeNode node, List<RecipeNode> leaves) {
            if (node.isLeaf()) {
                leaves.add(node);
                return;
            }
            for (RecipeNode child : node.getChildren()) {
                traversalLeaf(child, leaves);
            }
        }
    }

    static {
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }
}
