package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.widget.ImageData;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.uitls.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Comparator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RestaurantMenuScreen extends Screen {
    private static final Minecraft MINECRAFT;
    private static final Font FONT;
    private static final ImageData MENU;
    private static final ImageData CLIPBOARD;
    private static final ImageData PENCIL;
    private static final Color COMMON = new Color(178, 148, 135);
    private static final Color LESS_BLACK = new Color(0, 0, 0, 128);

    private final Rectangle menuArea;
    private final Rectangle[] menuEntryBtns;

    private final int maxPage;
    private final Map<Integer, MenuEntry> menuEntries;
    private final Order[] orders;

    private int currentPage;

    public RestaurantMenuScreen(ItemStack itemStack) {
        super(Component.empty());
        this.menuEntries = RestaurantMenuItem.getEntries(itemStack);
        this.maxPage = Mth.positiveCeilDiv(this.menuEntries.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(0) + 1, 4);
        this.orders = new Order[6];
        this.menuArea = new Rectangle(getScreenCenterX() + 27, getScreenCenterY() - 81, 132, 165);
        this.menuEntryBtns = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 40;
            int y = getScreenCenterY() - 53 + i * 32;
            this.menuEntryBtns[i] = new Rectangle(x, y, 94, 16);
        }
    }

    public static void open(ItemStack itemStack) {
        Minecraft.getInstance().setScreen(new RestaurantMenuScreen(itemStack));
    }

    private void addOrder(MenuEntry entry) {
        for (int i = 0; i < this.orders.length; i++) {
            if (this.orders[i] == null) {
                this.orders[i] = new Order(1, entry);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderBackground(graphics);
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();

        MENU.renderCentered(graphics, centerX + 107, centerY + 7);
        for (int i = 0; i < 4; i++) {
            int x = centerX + 40;
            int y = centerY - 53 + i * 32;
            graphics.fill(x, y + 15, x + 94, y + 16, COMMON.getRGB());
            if (this.menuEntries.containsKey(currentPage * 4 + i)) {
                MenuEntry entry = this.menuEntries.get(currentPage * 4 + i);
                Component text = Component.literal(entry.getName()).withStyle(ChatFormatting.BOLD);
                graphics.renderItem(entry.getInfo().output(), x + 1, y - 1);
                RenderUtil.drawString(graphics, font, text, x + 19, y + 4, 1F, COMMON.getRGB());
            }
        }
        RenderUtil.drawCenteredString(graphics, font, "%d/%d".formatted(currentPage + 1, maxPage),
                centerX + 90, centerY + 79, 0, 0.6F, COMMON.getRGB(), false);

        CLIPBOARD.renderCentered(graphics, getScreenCenterX() - 100, getScreenCenterY() - 5);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < this.menuEntryBtns.length; i++) {
            if (this.menuEntryBtns[i].contains(mouseX, mouseY) && this.orders[orders.length - 1] == null) {
                this.addOrder(menuEntries.get(currentPage * 4 + i));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.menuArea.contains(mouseX, mouseY)) {
            this.currentPage = Mth.clamp(currentPage - Mth.sign(delta), 0, maxPage - 1);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.menuArea.setLocation(getScreenCenterX() + 27, getScreenCenterY() - 81);
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 40;
            int y = getScreenCenterY() - 53 + i * 32;
            this.menuEntryBtns[i].setLocation(x, y);
        }
    }

    public static int getScreenCenterX(){
        return MINECRAFT.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return MINECRAFT.getWindow().getGuiScaledHeight() / 2;
    }

    private static class Order {
        private int count;
        private MenuEntry entry;

        public Order(int count, MenuEntry entry) {
            this.count = count;
            this.entry = entry;
        }
    }

    static {
        MINECRAFT = Minecraft.getInstance();
        FONT = MINECRAFT.font;
        MENU = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu.png"), 25, 11, 182, 185, 360, 358);
        CLIPBOARD = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard.png"), 76, 13, 167, 224, 359, 278);
        PENCIL = new ImageData(MaidRestaurant.modLoc("textures/gui/pencil.png"), 0, 0, 12, 12, 12, 12);
    }
}
