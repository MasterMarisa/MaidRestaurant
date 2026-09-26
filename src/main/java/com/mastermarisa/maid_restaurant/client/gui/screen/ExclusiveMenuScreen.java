package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.widget.ImageData;
import com.mastermarisa.maid_restaurant.data.menu.MenuEntry;
import com.mastermarisa.maid_restaurant.data.menu.OrderEntry;
import com.mastermarisa.maid_restaurant.data.menu.RecipeInfo;
import com.mastermarisa.maid_restaurant.item.RestaurantMenuItem;
import com.mastermarisa.maid_restaurant.item.UnboundMenuItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.SendExclusiveOrdersMessage;
import com.mastermarisa.maid_restaurant.network.message.StartSelectTargetsMessage;
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
public class ExclusiveMenuScreen extends Screen {
    private static final Minecraft MINECRAFT;
    private static final Font FONT;

    private static final ImageData MENU;
    private static final ImageData CLIPBOARD;
    private static final ImageData CROSS_MARK;
    private static final ImageData CROSS_MARK_HOVERED;
    private static final ImageData SNED_ORDER;
    private static final ImageData SNED_ORDER_HOVERED;
    private static final Color COMMON;
    private static final Color LESS_BLACK = new Color(0, 0, 0, 128);
    private static final int MAX_ORDER_COUNT;

    private final Rectangle menuArea;
    private final Rectangle[] menuEntryBtns;
    private final Rectangle[] orderEntryBtns;
    private final Rectangle[] cancelBtns;
    private final Rectangle orderBtn;
    private final Rectangle selectBtn;

    private final int maxPage;
    private final String restaurantId;
    private final Map<Integer, MenuEntry> menuEntries;
    private final OrderEntry[] orders;
    private int currentPage;

    public ExclusiveMenuScreen(ItemStack itemStack) {
        super(net.minecraft.network.chat.Component.empty());
        this.menuEntries = UnboundMenuItem.getEntries(itemStack);
        this.maxPage = Mth.positiveCeilDiv(this.menuEntries.keySet().stream().max(Comparator.comparingInt(a -> a)).orElse(0) + 1, 4);
        this.restaurantId = RestaurantMenuItem.getRestaurantId(itemStack);
        this.orders = new OrderEntry[MAX_ORDER_COUNT];
        this.menuArea = new Rectangle(132, 165);
        this.menuEntryBtns = new Rectangle[4];
        for (int i = 0; i < 4; i++) {
            this.menuEntryBtns[i] = new Rectangle(94, 16);
        }
        this.orderEntryBtns = new Rectangle[MAX_ORDER_COUNT];
        for (int i = 0; i < MAX_ORDER_COUNT; i++) {
            this.orderEntryBtns[i] = new Rectangle(108, 16);
        }
        this.cancelBtns = new Rectangle[this.orders.length];
        for (int i = 0; i < this.cancelBtns.length; i++) {
            this.cancelBtns[i] = new Rectangle(12, 12);
        }
        this.orderBtn = new Rectangle(20, 18);
        this.selectBtn = new Rectangle(20, 20);
        this.resize();
    }

    public static void open(ItemStack itemStack) {
        Minecraft.getInstance().setScreen(new ExclusiveMenuScreen(itemStack));
    }

    @Override
    protected void init() {
        super.init();
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
                net.minecraft.network.chat.Component text = net.minecraft.network.chat.Component.literal(entry.getName()).withStyle(ChatFormatting.BOLD);
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
            net.minecraft.network.chat.Component text = Component.literal(entry.getName()).withStyle(ChatFormatting.BOLD);
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

        RenderUtil.fill(graphics, this.selectBtn, LESS_BLACK.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.orders[0] != null) {
            if (this.orderBtn.contains(mouseX, mouseY)) {
                List<OrderEntry> orderEntryList = Arrays.stream(this.orders).filter(Objects::nonNull).toList();
                SendExclusiveOrdersMessage message = new SendExclusiveOrdersMessage(restaurantId, orderEntryList);
                NetworkHandler.sendToServer(message);
                MINECRAFT.setScreen(null);
                return true;
            }
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

        if (this.selectBtn.contains(mouseX, mouseY)) {
            NetworkHandler.sendToServer(new StartSelectTargetsMessage());
            MINECRAFT.setScreen(null);
            return true;
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

    private void resize() {
        int centerX = getScreenCenterX();
        int centerY = getScreenCenterY();
        this.menuArea.setLocation(centerX + 27, centerY - 81);
        for (int i = 0; i < 4; i++) {
            this.menuEntryBtns[i].setLocation(centerX + 40, centerY - 53 + i * 32);
        }
        for (int i = 0; i < this.orderEntryBtns.length; i++) {
            this.orderEntryBtns[i].setLocation(centerX - 164, centerY - 65 + i * 18);
        }
        for (int i = 0; i < this.cancelBtns.length; i++) {
            this.cancelBtns[i].setLocation(centerX - 44, centerY - 63 + i * 18);
        }
        this.orderBtn.setLocation(centerX - 164, centerY + 70);
        this.selectBtn.setLocation(centerX - 64, centerY + 66);
    }

    private void addOrder(MenuEntry entry) {
        for (int i = 0; i < MAX_ORDER_COUNT; i++) {
            if (this.orders[i] == null) {
                this.orders[i] = new OrderEntry(1, entry);
                break;
            }
        }
    }

    private void removeOrder(int index) {
        for (int i = index + 1; i < MAX_ORDER_COUNT; i++) {
            this.orders[i - 1] = this.orders[i];
        }
        this.orders[MAX_ORDER_COUNT - 1] = null;
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
        CROSS_MARK = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/cross_mark.png"), 0, 0, 12, 12, 12, 12);
        CROSS_MARK_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/cross_mark_hovered.png"), 0, 0, 12, 12, 12, 12);
        SNED_ORDER = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/send_order.png"), 0, 0, 20, 18, 20, 18);
        SNED_ORDER_HOVERED = new ImageData(MaidRestaurant.modLoc("textures/gui/restaurant_menu/send_order_hovered.png"), 0, 0, 20, 18, 20, 18);
        COMMON = new Color(178, 148, 135);
        MAX_ORDER_COUNT = 7;
    }
}
