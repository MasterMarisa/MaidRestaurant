package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.widget.ImageData;
import com.mastermarisa.maid_restaurant.client.gui.widget.UIElement;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.data.menu.RecipeInfo;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.BindMenuMessage;
import com.mastermarisa.maid_restaurant.network.message.UpdateUnboundMenuMessage;
import com.mastermarisa.maid_restaurant.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.uitls.ClientUtil;
import com.mastermarisa.maid_restaurant.uitls.RenderUtil;
import com.mojang.blaze3d.platform.InputConstants;
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
    private static final ImageData MENU_HOVERED;
    private static final ImageData MENU_BINDING;
    private static final ImageData CLIP_BOARD;
    private static final ImageData CLIP_BOARD_1;
    private static final ImageData CLIP_BOARD_2;
    private static final ImageData PENCIL;
    private static final ImageData ERASER;
    private static final ImageData ELLIPSIS;
    private static final ImageData CROSS_MARK;
    private static final ImageData CROSS_MARK_HOVERED;
    private static final ImageData TICK_MARK;
    private static final ImageData TICK_MARK_HOVERED;
    private static final Color LINE = new Color(178, 148, 135);
    private static final Color LESS_BLACK = new Color(0, 0, 0, 128);

    private final Rectangle guideSelectBtn;
    private final Rectangle[] menuEntryBtn;
    private final Rectangle saveBtn;
    private final Rectangle menuArea;
    private final Rectangle[] eraserBtns;
    private final Rectangle[] bindBtn;
    private final Rectangle cancelBtn;
    private final Rectangle confirmBtn;

    private final Map<Integer, MenuEntry> writtenEntries;
    private final GuideSelectOverlay overlay;
    private final EditBox nameEditBox;
    private final EditBox idEditBox;

    @Nullable
    private MenuEntry editingEntry;
    private int currentPage;
    private int currentIndex;
    private boolean binding;

    public UnboundMenuScreen(ItemStack itemStack, Player player) {
        super(Component.empty());
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();
        this.writtenEntries = UnboundMenuItem.getEntries(itemStack);
        this.overlay = new GuideSelectOverlay(player.getInventory(), this::onSelectRecipe);
        this.nameEditBox = new EditBox(font, centerX - 165, centerY + 78, 100, 14, Component.empty());
        this.nameEditBox.setMaxLength(30);
        this.nameEditBox.setBordered(false);
        this.nameEditBox.setCanLoseFocus(true);
        this.nameEditBox.setTextColor(LINE.getRGB());
        this.idEditBox = new EditBox(font, centerX - 30, centerY + 3, 60, 15, Component.empty());
        this.idEditBox.setMaxLength(30);
        this.idEditBox.setBordered(false);
        this.currentIndex = -1;
        this.guideSelectBtn = new Rectangle(centerX - 147, centerY - 58, 94, 124);
        this.menuEntryBtn = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = centerX + 45;
            int y = centerY - 57 + i * 32;
            this.menuEntryBtn[i] = new Rectangle(x, y, 94, 16);
        }
        this.saveBtn = new Rectangle(nameEditBox.getX() + 111, nameEditBox.getY() - 2, 12, 12);
        this.menuArea = new Rectangle(centerX + 27, centerY - 81, 132, 165);
        this.eraserBtns = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = centerX + 31;
            int y = centerY - 55 + i * 32;
            this.eraserBtns[i] = new Rectangle(x, y, 12, 12);
        }
        this.bindBtn = new Rectangle[6];
        this.bindBtn[0] = new Rectangle(centerX + 135, centerY + 72, 27, 26);
        this.bindBtn[1] = new Rectangle(centerX + 143, centerY + 54, 26, 18);
        this.bindBtn[2] = new Rectangle(centerX + 149, centerY + 37, 28, 18);
        this.bindBtn[3] = new Rectangle(centerX + 163, centerY + 22, 21, 15);
        this.bindBtn[4] = new Rectangle(centerX + 175, centerY + 12, 17, 11);
        this.bindBtn[5] = new Rectangle(centerX + 185, centerY + 4, 13, 9);
        this.cancelBtn = new Rectangle(centerX - 38, centerY + 22, 16, 16);
        this.confirmBtn = new Rectangle(centerX + 22, centerY + 22, 16, 16);
    }

    public static void open(ItemStack itemStack, Player player) {
        Minecraft.getInstance().setScreen(new UnboundMenuScreen(itemStack, player));
    }

    private void onSelectRecipe(RecipeNode root) {
        if (this.editingEntry == null) {
            this.editingEntry = new MenuEntry();
        }
        this.editingEntry.setRoot(root);
        this.removeWidget(this.nameEditBox);
        this.addRenderableWidget(this.nameEditBox);
    }

    @Override
    protected void init() {
        super.init();
        if (this.editingEntry != null && this.editingEntry.getRoot() != null) {
            this.removeWidget(this.nameEditBox);
            this.addRenderableWidget(this.nameEditBox);
        }
        if (this.binding) {
            this.removeWidget(this.idEditBox);
            this.addRenderableWidget(this.idEditBox);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderBackground(graphics);
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();

        if (binding) {
            MENU_BINDING.renderCentered(graphics, centerX - 5, centerY);
            this.idEditBox.render(graphics, mouseX, mouseY, partialTick);
            if (this.cancelBtn.contains(mouseX, mouseY)) {
                CROSS_MARK_HOVERED.render(graphics, this.cancelBtn.x, this.cancelBtn.y);
            } else {
                CROSS_MARK.render(graphics, this.cancelBtn.x, this.cancelBtn.y);
            }
            if (this.confirmBtn.contains(mouseX, mouseY)) {
                TICK_MARK_HOVERED.render(graphics, this.confirmBtn.x, this.confirmBtn.y);
            } else {
                TICK_MARK.render(graphics, this.confirmBtn.x, this.confirmBtn.y);
            }
            return;
        }

        boolean hovered = false;
        if (!overlay.active) {
            for (Rectangle rectangle : this.bindBtn) {
                if (rectangle.contains(mouseX, mouseY)) {
                    hovered = true;
                    break;
                }
            }
        }
        if (hovered) {
            MENU_HOVERED.renderCentered(graphics, getScreenCenterX() + 100, getScreenCenterY() + 10);
        } else {
            MENU.renderCentered(graphics, getScreenCenterX() + 100, getScreenCenterY() + 10);
        }
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
                Component text = Component.literal(entry.getName()).withStyle(ChatFormatting.BOLD);
                graphics.renderItem(entry.getInfo().output(), x + 1, y - 1);
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
            if (this.editingEntry.getRoot() == null) {
                CLIP_BOARD_2.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
                RenderUtil.drawString(graphics, font, Component.literal("+"), centerX - 105, centerY - 2, 2.0F, LINE.getRGB());
            } else {
                CLIP_BOARD.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
                int x = getScreenCenterX() - 164;
                int y = getScreenCenterY() - 67;
                Component text = Component.literal("菜品").withStyle(ChatFormatting.BOLD);
                Component text1 = Component.literal("烹饪方式").withStyle(ChatFormatting.BOLD);
                Component text2 = Component.literal("材料").withStyle(ChatFormatting.BOLD);
                RecipeInfo info = this.editingEntry.getInfo();

                graphics.drawString(font, text, x, y, LINE.getRGB(), false);
                y += 12;
                graphics.renderItem(info.output(), x, y);
                y += 20;
                graphics.drawString(font, text1, x, y , LINE.getRGB(), false);
                y += 12;
                if (info.workBlocks().size() > 6) {
                    for (int i = 0; i < 5; i++) {
                        graphics.renderItem(info.workBlocks().get(i), x + 20 * i, y);
                    }
                    ELLIPSIS.render(graphics, x + 90, y);
                }  else {
                    for (int i = 0; i < Math.min(6, info.workBlocks().size()); i++) {
                        graphics.renderItem(info.workBlocks().get(i), x + 20 * i, y);
                    }
                }
                y += 20;

                long gameTime = ClientUtil.gameTime();
                graphics.drawString(font, text2, x, y, LINE.getRGB(), false);
                y += 12;
                for (int i = 0; i < Math.min(6, info.inputs().size()); i++) {
                    RenderUtil.renderIngredient(graphics, info.inputs().get(i), x + 20 * i, y, gameTime, 20);
                }
                y += 20;
                if (info.inputs().size() > 6) {
                    if (info.inputs().size() > 12) {
                        for (int i = 6; i < 11; i++) {
                            RenderUtil.renderIngredient(graphics, info.inputs().get(i), x + 20 * (i - 6), y, gameTime, 20);
                        }
                        ELLIPSIS.render(graphics, x + 90, y);
                    } else {
                        for (int i = 6; i < Math.min(12, info.inputs().size()); i++) {
                            RenderUtil.renderIngredient(graphics, info.inputs().get(i), x + 20 * (i - 6), y, gameTime, 20);
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
        String text1 = this.idEditBox.getValue();
        boolean focused1 = this.idEditBox.isFocused();
        super.resize(pMinecraft, pWidth, pHeight);
        this.nameEditBox.setValue(text);
        this.nameEditBox.setFocused(focused);
        this.nameEditBox.setX(getScreenCenterX() - 100 - 65);
        this.nameEditBox.setY(getScreenCenterY() + 78);
        this.idEditBox.setValue(text1);
        this.idEditBox.setFocused(focused1);
        this.idEditBox.setX(getScreenCenterX() - 30);
        this.idEditBox.setY(getScreenCenterY() + 3);
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
        this.bindBtn[0].setLocation(getScreenCenterX() + 135, getScreenCenterY() + 72);
        this.bindBtn[1].setLocation(getScreenCenterX() + 143, getScreenCenterY() + 54);
        this.bindBtn[2].setLocation(getScreenCenterX() + 149, getScreenCenterY() + 37);
        this.bindBtn[3].setLocation(getScreenCenterX() + 163, getScreenCenterY() + 22);
        this.bindBtn[4].setLocation(getScreenCenterX() + 175, getScreenCenterY() + 12);
        this.bindBtn[5].setLocation(getScreenCenterX() + 185, getScreenCenterY() + 4);
        this.cancelBtn.setLocation(getScreenCenterX() - 38, getScreenCenterY() + 22);
        this.confirmBtn.setLocation(getScreenCenterX() + 22, getScreenCenterY() + 22);
        this.overlay.resize();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (this.binding) {
            if (this.idEditBox.isFocused() && !this.idEditBox.isHovered()) {
                this.idEditBox.setFocused(false);
            }

            if (this.cancelBtn.contains(mouseX, mouseY)) {
                this.binding = false;
                this.removeWidget(this.idEditBox);
                this.idEditBox.setValue("");
                return true;
            }

            if (this.confirmBtn.contains(mouseX, mouseY) && !this.idEditBox.getValue().isEmpty()) {
                BindMenuMessage message = new BindMenuMessage(this.idEditBox.getValue());
                NetworkHandler.sendToServer(message);
                minecraft.setScreen(null);
                return true;
            }
        }
        else if (overlay.active) {
            if (overlay.frame.contains(mouseX, mouseY)) {
                return overlay.onMouseClicked(mouseX, mouseY, button);
            } else {
                overlay.active = false;
                return true;
            }
        } else {
            if (this.nameEditBox.isFocused() && !this.nameEditBox.isHovered()) {
                this.nameEditBox.setFocused(false);
            }

            for (int i = 0; i < 4; i++) {
                if (this.menuEntryBtn[i].contains(mouseX, mouseY)) {
                    this.removeWidget(this.nameEditBox);
                    this.nameEditBox.setValue("");
                    this.currentIndex = this.currentPage * 4 + i;
                    this.editingEntry = this.writtenEntries.getOrDefault(this.currentIndex, new MenuEntry()).copy();
                    if (this.editingEntry.getRoot() != null) {
                        this.addRenderableWidget(this.nameEditBox);
                        this.nameEditBox.setValue(this.editingEntry.getName());
                        this.nameEditBox.setFocused(true);
                    }
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
                        this.removeWidget(this.nameEditBox);
                    }
                    UpdateUnboundMenuMessage.Remove message = new UpdateUnboundMenuMessage.Remove(index);
                    NetworkHandler.sendToServer(message);
                    return true;
                }
            }

            if (this.editingEntry != null && this.editingEntry.getRoot() == null) {
                if (this.guideSelectBtn.contains(mouseX, mouseY)) {
                    this.overlay.active = true;
                    return true;
                }
            }

            if (this.editingEntry != null && this.editingEntry.getRoot() != null) {
                if (this.saveBtn.contains(mouseX, mouseY)) {
                    this.editingEntry.setName(this.nameEditBox.getValue());
                    this.writtenEntries.put(this.currentIndex, this.editingEntry.copy());
                    this.nameEditBox.setFocused(false);
                    UpdateUnboundMenuMessage.Update message = new UpdateUnboundMenuMessage.Update(this.currentIndex, this.writtenEntries.get(this.currentIndex).serializeNBT());
                    NetworkHandler.sendToServer(message);
                    return true;
                }
            }

            for (Rectangle frame : this.bindBtn) {
                if (frame.contains(mouseX, mouseY)) {
                    this.binding = true;
                    this.removeWidget(this.nameEditBox);
                    this.removeWidget(this.idEditBox);
                    this.addRenderableWidget(this.idEditBox);
                    this.idEditBox.setFocused(true);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_ESCAPE) {
            if (this.nameEditBox.isFocused()) {
                this.nameEditBox.setFocused(false);
            }

            if (this.idEditBox.isFocused()) {
                this.idEditBox.setFocused(false);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

    public static class GuideSelectOverlay extends UIElement {
        private static final Color BG = new Color(0, 0, 0, 128);
        private final List<Slot> slots;
        private final Consumer<RecipeNode> callback;
        public boolean active;

        public GuideSelectOverlay(Inventory inventory, Consumer<RecipeNode> callback) {
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
                if (slot.frame.contains(mouseX, mouseY)) {
                    ItemStack stack = slot.getItem();
                    if (stack.is(ModItems.COOKING_GUIDE.get()) && CookingGuideItem.hasRecipe(stack)) {
                        CompoundTag tag = CookingGuideItem.getRecipeRoot(slot.getItem());
                        RecipeNode root = tag.isEmpty() ? new RecipeNode() : RecipeNode.fromNBT(tag);
                        this.callback.accept(root);
                        this.active = false;
                        return true;
                    }
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
        MENU_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/unbound_menu1.png"), 0, 0, 210, 216, 360, 358);
        MENU_BINDING = new ImageData(MaidRestaurant.modLoc("textures/gui/unbound_menu2.png"), 50, 40, 178, 198, 275, 263);
        CLIP_BOARD = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard.png"), 76, 13, 167, 224, 359, 278);
        CLIP_BOARD_1 = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard1.png"), 0, 0, 240, 120, 240, 120);
        CLIP_BOARD_2 = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard2.png"), 76, 13, 167, 224, 359, 278);
        PENCIL = new ImageData(MaidRestaurant.modLoc("textures/gui/pencil.png"), 0, 0, 12, 12, 12, 12);
        ERASER = new ImageData(MaidRestaurant.modLoc("textures/gui/eraser.png"), 0, 0, 12, 12, 12, 12);
        ELLIPSIS = new ImageData(MaidRestaurant.modLoc("textures/gui/ellipsis.png"), 0, 0, 16,16, 16, 16);
        CROSS_MARK = new ImageData(MaidRestaurant.modLoc("textures/gui/cross_mark.png"), 0, 0, 16, 16, 16, 16);
        CROSS_MARK_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/cross_mark_hovered.png"), 0, 0, 16, 16, 16, 16);
        TICK_MARK = new ImageData(MaidRestaurant.modLoc("textures/gui/tick_mark.png"), 0, 0, 16, 16, 16, 16);
        TICK_MARK_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/tick_mark_hovered.png"), 0, 0, 16, 16, 16, 16);
    }
}
