package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.RecipeMaterials;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.*;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.network.SendOrderPayload;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIConfirmTag extends UIElement {
    private final UIImage bg;
    private final UIConfirmButton btn;
    private final UICycleButton cycle;
    private final UIStockingModeButton stocking;

    public UIConfirmTag(OrderingScreen screen) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.orderedTagImage);
        cycle = new UICycleButton();
        stocking = new UIStockingModeButton();
        btn = new UIConfirmButton(screen,cycle,stocking);

        children = new ArrayList<>(List.of(bg,btn,cycle,stocking));
        resize();
    }

    public void resize() {
        bg.setCenter(getCenterX(),getCenterY());
        btn.setMaxX(bg.getMaxX() - 8);
        btn.setCenterY(getCenterY());
        cycle.setMaxX(btn.getMinX() - 3);
        cycle.setCenterY(getCenterY());
        stocking.setMaxX(cycle.getMinX() - 3);
        stocking.setCenterY(getCenterY());
    }

    @ParametersAreNonnullByDefault
    public static class UICycleButton extends UIButton {
        private boolean powered;
        private final UIImage poweredImg;
        private final UIImage unpoweredImg;

        public UICycleButton() {
            super(new Rectangle(14,16), (button) -> ((UICycleButton)button).trigger(), 0);
            poweredImg = new UIImage(UIConst.cycle_powered);
            unpoweredImg = new UIImage(UIConst.cycle_unpowered);
            poweredImg.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.cycle_on"));
            unpoweredImg.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.cycle_off"));
        }

        @Override
        protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            if (powered) {
                poweredImg.setCenter(getCenterX(),getCenterY());
                UIElement.render(graphics,poweredImg,mouseX,mouseY);
            } else {
                unpoweredImg.setCenter(getCenterX(),getCenterY());
                UIElement.render(graphics,unpoweredImg,mouseX,mouseY);
            }
        }

        public void trigger() {
            powered = !powered;
        }

        public boolean getState() {
            return powered;
        }
    }

    @ParametersAreNonnullByDefault
    public static class UIStockingModeButton extends UIButton {
        public int type;
        private final UIItemStack disabled;
        private final UIItemStack insertable;
        private final UIItemStack spaceEnough;

        public UIStockingModeButton() {
            super(new Rectangle(16,16), (button) -> ((UIStockingModeButton)button).trigger(), 0);
            disabled = new UIItemStack(new ItemStack(ModItems.TABLE_OAK.get()));
            insertable = new UIItemStack(new ItemStack(Items.HOPPER));
            spaceEnough = new UIItemStack(new ItemStack(Items.CHEST));
            disabled.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_unlimited"));
            insertable.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_insertable"));
            spaceEnough.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_space_enough"));
        }

        @Override
        protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            switch (type) {
                case 0 -> {
                    disabled.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,disabled,mouseX,mouseY);
                }
                case 1 -> {
                    insertable.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,insertable,mouseX,mouseY);
                }
                case 2 -> {
                    spaceEnough.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,spaceEnough,mouseX,mouseY);
                }
            }
        }

        public void trigger() {
            type++;
            if (type > 2) type = 0;
        }
    }

    public static class UIConfirmButton extends UIButton {
        private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/confirm.png");

        /** Tooltip lines shown before the shortage list is truncated. */
        private static final int MAX_WARNING_LINES = 8;

        /** Lines listed in chat when the player confirms an order they cannot supply. */
        private static final int MAX_CHAT_LINES = 8;

        private static final int REFRESH_TICKS = 10;

        private final OrderingScreen screen;
        private final UICycleButton cycle;
        private final UIStockingModeButton stocking;

        private long lastRefreshTick = Long.MIN_VALUE;

        public UIConfirmButton(OrderingScreen screen, UICycleButton cycle, UIStockingModeButton stocking) {
            super(new Rectangle(11,7),(button) -> ((UIConfirmButton) button).sendOrder(),0);
            this.screen = screen;
            this.cycle = cycle;
            this.stocking = stocking;
        }

        @Override
        protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            refreshWarning();
            if (!screen.orders.isEmpty()) {
                graphics.blit(texture, getMinX(), getMinY(), 0,0,11,7,11,7);
            }
        }

        /** Keeps the "you are missing X" tooltip current without re-scanning the world every frame. */
        private void refreshWarning() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || screen.orders.isEmpty()) return;

            long now = minecraft.level.getGameTime();
            if (now - lastRefreshTick < REFRESH_TICKS) return;
            lastRefreshTick = now;

            this.tooltip = new ArrayList<>(RecipeMaterials.describe(
                    screen.missingMaterials(MAX_WARNING_LINES + 1),
                    MAX_WARNING_LINES
            ));
        }

        public void sendOrder() {
            MaidRestaurant.LOGGER.debug("SEND ORDER");
            List<OrderingScreen.Order> orders = screen.orders;

            // The order is still sent - the player may be restocking on purpose and the maids may
            // draw from containers this check cannot see - but never silently, which is what used to
            // happen: an order the player had no materials for simply sat there doing nothing.
            warnIfMaterialsMissing();

            String[] recipeIDs = new String[orders.size()];
            String[] recipeTypes = new String[orders.size()];
            int[] counts = new int[orders.size()];
            long[] tables = new long[screen.targets.size()];
            byte[] attributes = new byte[] {(byte) (cycle.powered ? 1 : 0), (byte) stocking.type};

            for (int i = 0;i < orders.size();i++) {
                OrderingScreen.Order order = orders.get(i);
                recipeIDs[i] = order.data.ID.toString();
                recipeTypes[i] = CookTasks.getUID(order.data.type);
                counts[i] = order.count;
            }

            for (int i = 0;i < screen.targets.size();i++) {
                tables[i] = screen.targets.get(i).asLong();
            }

            SendOrderPayload payload = new SendOrderPayload(recipeIDs,recipeTypes,counts,tables,attributes);
            PacketDistributor.sendToServer(payload);

            screen.close();
        }

        private void warnIfMaterialsMissing() {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            List<ItemStack> missing = screen.missingMaterials(MAX_CHAT_LINES + 1);
            if (missing.isEmpty()) return;

            // This check can only see what is still in storage: a pot's contents are never sent to the
            // client, so ingredients a maid has already loaded are invisible here. When a cooker stands
            // nearby, say so, otherwise the player is told to fetch items they have already handed over.
            String header = RecipeMaterials.isCookingBlockNear()
                    ? "message.maid_restaurant.materials_missing_cooking"
                    : "message.maid_restaurant.materials_missing";

            player.displayClientMessage(
                    Component.translatable(header).withStyle(ChatFormatting.GOLD),
                    false
            );
            for (Component line : RecipeMaterials.describe(missing, MAX_CHAT_LINES))
                player.displayClientMessage(line.copy().withStyle(ChatFormatting.YELLOW), false);

            // State the size of the pool this judgement was made from. Without it a player who has the
            // items in a chest just outside the scan radius cannot tell a radius problem from a bug.
            player.displayClientMessage(
                    Component.translatable("message.maid_restaurant.materials_scan",
                            RecipeMaterials.lastPoolSize(),
                            RecipeMaterials.lastContainerItems(),
                            RecipeMaterials.scanRange()).withStyle(ChatFormatting.GRAY),
                    false
            );
        }
    }
}
