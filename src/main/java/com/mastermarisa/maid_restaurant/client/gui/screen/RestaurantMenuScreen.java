package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.widget.ImageData;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.menu.RecipeInfo;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.SendOrderMessage;
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
import java.util.*;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RestaurantMenuScreen extends Screen {
    private static final Minecraft MINECRAFT;
    private static final Font FONT;
    private static final ImageData MENU;
    private static final ImageData CLIPBOARD;
    private static final ImageData ERASER;
    private static final ImageData CROSS_MARK;
    private static final ImageData CROSS_MARK_HOVERED;
    private static final ImageData SNED_ORDER;
    private static final ImageData SNED_ORDER_HOVERED;
    private static final Color COMMON = new Color(178, 148, 135);
    private static final Color LESS_BLACK = new Color(0, 0, 0, 128);

    private final Rectangle menuArea;
    private final Rectangle[] menuEntryBtns;
    private final Rectangle[] orderEntryBtns;
    private final Rectangle orderBtn;
    private final Rectangle[] cancelBtns;

    private final int maxPage;
    private final String restaurantId;
    private final Map<Integer, MenuEntry> menuEntries;
    private final OrderEntry[] orders;

    private int currentPage;

    public RestaurantMenuScreen(ItemStack itemStack) {
        super(Component.empty());
        this.menuEntries = RestaurantMenuItem.getEntries(itemStack);
        this.maxPage = Mth.positiveCeilDiv(this.menuEntries.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(0) + 1, 4);
        this.restaurantId = RestaurantMenuItem.getRestaurantId(itemStack);
        this.orders = new OrderEntry[7];
        this.menuArea = new Rectangle(getScreenCenterX() + 27, getScreenCenterY() - 81, 132, 165);
        this.menuEntryBtns = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            int x = getScreenCenterX() + 40;
            int y = getScreenCenterY() - 53 + i * 32;
            this.menuEntryBtns[i] = new Rectangle(x, y, 94, 16);
        }
        this.orderEntryBtns = new Rectangle[this.orders.length];
        for (int i = 0; i < this.orderEntryBtns.length; i++) {
            int x = getScreenCenterX() - 164;
            int y = getScreenCenterY() - 65 + i * 18;
            this.orderEntryBtns[i] = new Rectangle(x, y, 108, 16);
        }
        this.orderBtn = new Rectangle(getScreenCenterX() - 164, getScreenCenterY() + 66, 24, 24);
        this.cancelBtns = new Rectangle[this.orders.length];
        for (int i = 0; i < this.cancelBtns.length; i++) {
            int x = getScreenCenterX() - 44;
            int y = getScreenCenterY() - 63 + i * 18;
            this.cancelBtns[i] = new Rectangle(x, y, 12, 12);
        }
    }

    public static void open(ItemStack itemStack) {
        Minecraft.getInstance().setScreen(new RestaurantMenuScreen(itemStack));
    }

    private void addOrder(MenuEntry entry) {
        for (int i = 0; i < this.orders.length; i++) {
            if (this.orders[i] == null) {
                this.orders[i] = new OrderEntry(1, entry);
                break;
            }
        }
    }

    private void removeOrder(int index) {
        for (int i = index + 1; i < this.orders.length; i++) {
            this.orders[i - 1] = this.orders[i];
        }
        this.orders[this.orders.length - 1] = null;
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
        int x = getScreenCenterX() - 164;
        int y = getScreenCenterY() - 65;
        for (int i = 0; i < this.orders.length; i++) {
            OrderEntry order = this.orders[i];
            if (order == null) {
                break;
            }
            MenuEntry entry = order.getEntry();
            RecipeInfo info = entry.getInfo();
            graphics.renderItem(info.output(), x, y + 18 * i);
            Component text = Component.literal(entry.getName()).withStyle(ChatFormatting.BOLD);
            graphics.drawString(FONT, text, x + 17, y + 5 + 18 * i, COMMON.getRGB(), false);
            graphics.drawString(FONT, "x%d".formatted(order.getCount()), x + 100, y + 5 + 18 * i, COMMON.getRGB(), false);
            if (this.cancelBtns[i].contains(mouseX, mouseY)) {
                CROSS_MARK_HOVERED.render(graphics, cancelBtns[i].x, cancelBtns[i].y);
            } else {
                CROSS_MARK.render(graphics, cancelBtns[i].x, cancelBtns[i].y);
            }
        }

        if (orders[0] != null) {
            if (this.orderBtn.contains(mouseX, mouseY)) {
                SNED_ORDER_HOVERED.render(graphics, orderBtn.x, orderBtn.y);
            } else {
                SNED_ORDER.render(graphics, orderBtn.x, orderBtn.y);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.orders[0] != null && this.orderBtn.contains(mouseX, mouseY)) {
            List<OrderEntry> orderEntryList = Arrays.stream(this.orders).filter(Objects::nonNull).toList();
            SendOrderMessage message = new SendOrderMessage(restaurantId, orderEntryList);
            NetworkHandler.sendToServer(message);
            MINECRAFT.setScreen(null);
            return true;
        }
        for (int i = 0; i < this.menuEntryBtns.length; i++) {
            if (this.menuEntryBtns[i].contains(mouseX, mouseY) && this.menuEntries.containsKey(currentPage * 4 + i)
                    && this.orders[orders.length - 1] == null) {
                this.addOrder(menuEntries.get(currentPage * 4 + i));
                return true;
            }
        }
        for (int i = 0; i < this.cancelBtns.length; i++) {
            if (this.orders[i] != null && this.cancelBtns[i].contains(mouseX, mouseY)) {
                this.removeOrder(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.menuArea.contains(mouseX, mouseY)) {
            this.currentPage = Mth.clamp(currentPage - Mth.sign(delta), 0, maxPage - 1);
            return true;
        }
        for (int i = 0; i < this.orderEntryBtns.length; i++) {
            if (this.orderEntryBtns[i].contains(mouseX, mouseY) && this.orders[i] != null) {
                OrderEntry order = this.orders[i];
                order.setCount(Mth.clamp(order.getCount() + (int) delta, 1, 64));
                return true;
            }
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
        for (int i = 0; i < this.orderEntryBtns.length; i++) {
            int x = getScreenCenterX() - 164;
            int y = getScreenCenterY() - 65 + i * 18;
            this.orderEntryBtns[i].setLocation(x, y);
        }
        this.orderBtn.setLocation(getScreenCenterX() - 164, getScreenCenterY() + 66);
        for (int i = 0; i < this.cancelBtns.length; i++) {
            int x = getScreenCenterX() - 44;
            int y = getScreenCenterY() - 63 + i * 18;
            this.cancelBtns[i].setLocation(x, y);
        }
    }

    public static int getScreenCenterX(){
        return MINECRAFT.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return MINECRAFT.getWindow().getGuiScaledHeight() / 2;
    }

    static {
        MINECRAFT = Minecraft.getInstance();
        FONT = MINECRAFT.font;
        MENU = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu.png"), 25, 11, 182, 185, 360, 358);
        CLIPBOARD = new ImageData(MaidRestaurant.modLoc("textures/gui/clipboard.png"), 76, 13, 167, 224, 359, 278);
        ERASER = new ImageData(MaidRestaurant.modLoc("textures/gui/eraser1.png"), 0, 0, 12, 12, 12, 12);
        CROSS_MARK = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/cross_mark.png"), 0, 0, 12, 12, 12, 12);
        CROSS_MARK_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/cross_mark_hovered.png"), 0, 0, 12, 12, 12, 12);
        SNED_ORDER = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/send_order.png"), 0, 0, 24, 24, 24, 24);
        SNED_ORDER_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/send_order_hovered.png"), 0, 0, 24, 24, 24, 24);
    }
}
