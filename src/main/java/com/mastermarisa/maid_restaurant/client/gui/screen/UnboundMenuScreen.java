package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.client.gui.widget.ImageData;
import com.mastermarisa.maid_restaurant.client.gui.widget.UIElement;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.SaveUnboundMenuMessage;
import com.mastermarisa.maid_restaurant.recipe.IngredientStack;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.tree.RecipeStep;
import com.mastermarisa.maid_restaurant.uitls.ClientUtil;
import com.mastermarisa.maid_restaurant.uitls.IngredientUtil;
import com.mastermarisa.maid_restaurant.uitls.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class UnboundMenuScreen extends Screen {
    private static final Minecraft minecraft;
    private static final Font font;
    private static final ImageData MENU;
    private static final ImageData CLIP_BOARD;
    private static final ImageData CLIP_BOARD_1;
    private static final ImageData CLIP_BOARD_2;
    private static final ImageData PENCIL;
    private static final ImageData ERASER;
    private static final ImageData ELLIPSIS;
    private static final Color lessBlack = new Color(0, 0, 0, 128);
    private static final Color leastBlack = new Color(0, 0, 0, 16);
    private static final Color LINE = new Color(178, 148, 135);

    private final ItemStack itemStack;
    private final Player player;
    private final Map<Integer, MenuEntry> writtenEntries;
    private final GuideSelectOverlay overlay;
    private final EditBox nameEditBox;

    private final Rectangle guideSelectBtn;
    private final Rectangle[] menuEntryBtn;
    private final Rectangle saveBtn;
    private final Rectangle menuArea;
    private final Rectangle[] eraserBtns;

    @Nullable
    private MenuEntry editingEntry;
    private int currentPage;
    private int currentIndex;

    public UnboundMenuScreen(ItemStack itemStack, Player player) {
        super(Component.empty());
        this.itemStack = itemStack;
        this.player = player;
        this.writtenEntries = UnboundMenuItem.deserializeMap(UnboundMenuItem.getMenuEntries(itemStack));
        this.overlay = new GuideSelectOverlay(player.getInventory(), this::onSelectRecipe);
        this.nameEditBox = new EditBox(font, getScreenCenterX() - 165, getScreenCenterY() + 78, 100, 14, Component.empty());
        this.nameEditBox.setMaxLength(30);
        this.nameEditBox.setBordered(false);
        this.nameEditBox.setCanLoseFocus(true);
        this.nameEditBox.setTextColor(LINE.getRGB());
        this.currentIndex = -1;
        this.guideSelectBtn = new Rectangle(getScreenCenterX() - 147, getScreenCenterY() - 58, 94, 124);
        this.menuEntryBtn = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 45;
            int y = getScreenCenterY() - 57 + i * 32;
            this.menuEntryBtn[i] = new Rectangle(x, y, 94, 16);
        }
        this.saveBtn = new Rectangle(nameEditBox.getX() + 111, nameEditBox.getY() - 2, 12, 12);
        this.menuArea = new Rectangle(getScreenCenterX() + 27, getScreenCenterY() - 81, 132, 165);
        this.eraserBtns = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 31;
            int y = getScreenCenterY() - 55 + i * 32;
            this.eraserBtns[i] = new Rectangle(x, y, 12, 12);
        }
    }

    public static void open(ItemStack itemStack, Player player) {
        Minecraft.getInstance().setScreen(new UnboundMenuScreen(itemStack, player));
    }

    private void onSelectRecipe(RecipeInfo info) {
        if (this.editingEntry == null) {
            this.editingEntry = new MenuEntry();
        }
        this.editingEntry.info = info;
    }

    private void sendSyncMessage() {
        CompoundTag tag = UnboundMenuItem.serializeMap(this.writtenEntries);
        SaveUnboundMenuMessage message = new SaveUnboundMenuMessage(tag);
        NetworkHandler.sendToServer(message);
    }

    @Override
    protected void init() {
        super.init();
        this.removeWidget(this.nameEditBox);
        this.addRenderableWidget(this.nameEditBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderBackground(graphics);
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();
        MENU.renderCentered(graphics, getScreenCenterX() + 100, getScreenCenterY() + 10);
        for (int i = 0; i < 4; i++) {
            int x = centerX + 45;
            int y = centerY - 57 + i * 32;
            graphics.fill(x, y + 15, x + 94, y + 16, LINE.getRGB());
            if (this.currentIndex == currentPage * 4 + i) {
                // 标记渲染
                Component icon = Component.literal("<").withStyle(ChatFormatting.BOLD);
                RenderUtil.drawString(graphics, font, icon, x + 97, y + 6, 0.8F, LINE.getRGB());
            }

            if (writtenEntries.containsKey(currentPage * 4 + i)) {
                MenuEntry entry = writtenEntries.get(currentPage * 4 + i);
                Component text = entry.name.copy().withStyle(ChatFormatting.BOLD);
                graphics.renderItem(entry.info.output.getItems()[0], x + 1, y - 1);
                RenderUtil.drawString(graphics, font, text, x + 19, y + 4, 1F, LINE.getRGB());
                ERASER.render(graphics, x - 14, y);
            }
        }
        RenderUtil.drawCenteredString(graphics, font, String.valueOf(currentPage + 1),
                centerX + 94, centerY + 76, 0, 0.6F, LINE.getRGB(), false);

        if (this.editingEntry == null) {
            CLIP_BOARD.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
            Component text = Component.literal("请选择需要编辑的条目").withStyle(ChatFormatting.BOLD);
            graphics.drawString(font, text, getScreenCenterX() - 100 - font.width(text) / 2, getScreenCenterY(), LINE.getRGB(), false);
        } else {
            if (this.editingEntry.info == null) {
                CLIP_BOARD_2.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
                RenderUtil.drawString(graphics, font, Component.literal("+"), centerX - 105, centerY - 2, 2.0F, LINE.getRGB());
            } else {
                CLIP_BOARD.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
                int x = getScreenCenterX() - 100 - 84 + 20;
                int y = getScreenCenterY() - 112 + 45;
                Component text = Component.literal("菜品").withStyle(ChatFormatting.BOLD);
                Component text1 = Component.literal("烹饪方式").withStyle(ChatFormatting.BOLD);
                Component text2 = Component.literal("材料").withStyle(ChatFormatting.BOLD);
                RecipeInfo info = this.editingEntry.info;

                graphics.drawString(font, text, x, y, LINE.getRGB(), false);
                y += 12;
                graphics.renderItem(info.output.getItems()[0], x, y);
                y += 20;
                graphics.drawString(font, text1, x, y , LINE.getRGB(), false);
                y += 12;
                if (info.workBlocks.size() > 6) {
                    for (int i = 0; i < 5; i++) {
                        graphics.renderItem(info.workBlocks.get(i), x + 20 * i, y);
                    }
                    ELLIPSIS.render(graphics, x + 90, y);
                }  else {
                    for (int i = 0; i < Math.min(6, info.workBlocks.size()); i++) {
                        graphics.renderItem(info.workBlocks.get(i), x + 20 * i, y);
                    }
                }
                y += 20;

                long gameTime = ClientUtil.gameTime();
                graphics.drawString(font, text2, x, y, LINE.getRGB(), false);
                y += 12;
                for (int i = 0; i < Math.min(6, info.inputs.size()); i++) {
                    RenderUtil.renderIngredient(graphics, info.inputs.get(i).getIngredient(), x + 20 * i, y, gameTime, 20);
                }
                y += 20;
                if (info.inputs.size() > 6) {
                    if (info.inputs.size() > 12) {
                        for (int i = 6; i < 11; i++) {
                            RenderUtil.renderIngredient(graphics, info.inputs.get(i).getIngredient(), x + 20 * (i - 6), y, gameTime, 20);
                        }
                        ELLIPSIS.render(graphics, x + 90, y);
                    } else {
                        for (int i = 6; i < Math.min(12, info.inputs.size()); i++) {
                            RenderUtil.renderIngredient(graphics, info.inputs.get(i).getIngredient(), x + 20 * (i - 6), y, gameTime, 20);
                        }
                    }
                }

                graphics.fill(nameEditBox.getX() - 2, nameEditBox.getY() + 9, nameEditBox.getX() + 105, nameEditBox.getY() + 10, LINE.getRGB());
                this.nameEditBox.render(graphics, mouseX, mouseY, partialTick);

                PENCIL.render(graphics, saveBtn.x, saveBtn.y);
            }
        }

        if (this.overlay.active) {
            this.overlay.render(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String text = this.nameEditBox.getValue();
        boolean focused = this.nameEditBox.isFocused();
        super.resize(pMinecraft, pWidth, pHeight);
        this.nameEditBox.setValue(text);
        this.nameEditBox.setFocused(focused);
        this.nameEditBox.setX(getScreenCenterX() - 100 - 65);
        this.nameEditBox.setY(getScreenCenterY() + 78);
        this.guideSelectBtn.setLocation(getScreenCenterX() - 147, getScreenCenterY() - 58);
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() - 5 + 50;
            int y = getScreenCenterY() - 98 + 41 + i * 32;
            this.menuEntryBtn[i].setLocation(x, y);
        }
        this.saveBtn.setLocation(nameEditBox.getX() + 111, nameEditBox.getY() - 2);
        this.menuArea.setLocation(getScreenCenterX() + 100 - 73, getScreenCenterY() - 81);
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 31;
            int y = getScreenCenterY() - 55 + i * 32;
            this.eraserBtns[i].setLocation(x, y);
        }
        this.overlay.resize();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (overlay.active) {
            if (overlay.frame.contains(mouseX, mouseY)) {
                return overlay.onMouseClicked(mouseX, mouseY, button);
            } else {
                overlay.active = false;
                return true;
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (this.menuEntryBtn[i].contains(mouseX, mouseY)) {
                    this.currentIndex = this.currentPage * 4 + i;
                    this.editingEntry = this.writtenEntries.getOrDefault(this.currentIndex, new MenuEntry()).copy();
                    this.nameEditBox.setValue(this.editingEntry.name.getString());
                    this.nameEditBox.setFocused(false);
                    return true;
                }
            }

            for (int i = 0; i < 4; i++) {
                int index = currentPage * 4 + i;
                if (this.writtenEntries.containsKey(index) && this.eraserBtns[i].contains(mouseX, mouseY)) {
                    this.writtenEntries.remove(index);
                    if (this.currentIndex == index) {
                        this.editingEntry = new MenuEntry();
                        this.nameEditBox.setValue("");
                    }
                    sendSyncMessage();
                    return true;
                }
            }

            if (this.editingEntry != null && this.editingEntry.info == null) {
                if (this.guideSelectBtn.contains(mouseX, mouseY)) {
                    this.overlay.active = true;
                    return true;
                }
            }

            if (this.editingEntry != null && this.editingEntry.info != null) {
                if (this.saveBtn.contains(mouseX, mouseY)) {
                    this.editingEntry.name = Component.literal(this.nameEditBox.getValue());
                    this.writtenEntries.put(this.currentIndex, this.editingEntry.copy());
                    this.nameEditBox.setFocused(false);
                    this.sendSyncMessage();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!this.overlay.active && this.menuArea.contains(mouseX, mouseY)) {
            this.currentPage = Math.max(0, currentPage - Mth.sign(delta));
        }
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

    public static class MenuEntry implements INBTSerializable<CompoundTag> {
        public RecipeInfo info;
        public Component name;

        public MenuEntry() {
            this.info = null;
            this.name = Component.empty();
        }

        public MenuEntry(RecipeInfo info, Component name) {
            this.info = info;
            this.name = name;
        }

        public MenuEntry copy() {
            return new MenuEntry(this.info, this.name.copy());
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.info != null) {
                tag.put("info", info.root.serializeNBT());
            }
            tag.putString("name", name.getString());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("info")) {
                RecipeNode root = RecipeNode.fromNBT(tag.getCompound("info"));
                this.info = RecipeInfo.fromNode(root);
            }
            if (tag.contains("name")) {
                this.name = Component.literal(tag.getString("name"));
            }
        }
    }

    public static class RecipeInfo {
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

    public static class GuideSelectOverlay extends UIElement {
        private static final Color BG = new Color(0, 0, 0, 128);
        private final List<Slot> slots;
        private final Consumer<RecipeInfo> callback;
        public boolean active;

        public GuideSelectOverlay(Inventory inventory, Consumer<RecipeInfo> callback) {
            super(new Rectangle(240, 120));
            this.slots = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                this.slots.add(new Slot(inventory, i));
            }
            this.callback = callback;
            this.resize();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            {
                pose.translate(0, 0, 400);
                var window = minecraft.getWindow();
                graphics.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), BG.getRGB());
                CLIP_BOARD_1.renderCentered(graphics, getScreenCenterX() - 15, getScreenCenterY());
                for (Slot slot : slots) {
                    slot.render(graphics, mouseX, mouseY);
                }
            }
            pose.popPose();
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            for (Slot slot : slots) {
                if (slot.frame.contains(mouseX, mouseY) && slot.getItem().is(ModItems.COOKING_GUIDE.get())) {
                    CompoundTag tag = CookingGuideItem.getRecipeRoot(slot.getItem());
                    RecipeNode root = tag.isEmpty() ? new RecipeNode() : RecipeNode.fromNBT(tag);
                    this.callback.accept(RecipeInfo.fromNode(root));
                    this.active = false;
                    return true;
                }
            }
            return super.onMouseClicked(mouseX, mouseY, button);
        }

        public void resize() {
            this.setCenter(getScreenCenterX(), getScreenCenterY());

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 9; i++) {
                    Slot slot = this.slots.get(i + (j + 1) * 9);
                    slot.setMinX(frame.x + 41 + i * 18);
                    slot.setMinY(frame.y + 20 + j * 18);
                }
            }

            for (int i = 0; i < 9; i++) {
                Slot slot = this.slots.get(i);
                slot.setMinX(frame.x + 41 + i * 18);
                slot.setMinY(frame.y + 85);
            }
        }

        private static class Slot extends UIElement {
            private static final Color HIGHLIGHT = new Color(255, 255, 255, 64);
            private final Inventory inventory;
            private final int index;

            public Slot(Inventory inventory, int index) {
                super(new Rectangle(16, 16));
                this.inventory = inventory;
                this.index = index;
            }

            public ItemStack getItem() {
                return this.inventory.getItem(index);
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY) {
                super.render(graphics, mouseX, mouseY);
                graphics.renderItem(inventory.getItem(index), frame.x, frame.y);
                graphics.renderItemDecorations(font, inventory.getItem(index), frame.x, frame.y);
                if (frame.contains(mouseX, mouseY)) {
                    PoseStack pose = graphics.pose();
                    pose.pushPose();
                    {
                        pose.translate(0, 0, 200);
                        graphics.fill(frame.x, frame.y, getMaxX(), getMaxY(), HIGHLIGHT.getRGB());
                    }
                    pose.popPose();
                }
            }
        }
    }

    static {
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
        MENU = new ImageData(MaidRestaurant.modLoc("textures/gui/unbound_menu.png"), 0, 0, 210, 216, 360, 358);
        CLIP_BOARD = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard.png"), 76, 13, 167, 224, 359, 278);
        CLIP_BOARD_1 = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard1.png"), 0, 0, 240, 120, 240, 120);
        CLIP_BOARD_2 = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard2.png"), 76, 13, 167, 224, 359, 278);
        PENCIL = new ImageData(MaidRestaurant.modLoc("textures/gui/pencil.png"), 0, 0, 12, 12, 12, 12);
        ERASER = new ImageData(MaidRestaurant.modLoc("textures/gui/eraser.png"), 0, 0, 12, 12, 12, 12);
        ELLIPSIS = new ImageData(MaidRestaurant.modLoc("textures/gui/ellipsis.png"), 0, 0, 16,16, 16, 16);
    }
}
