package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.element.UIButton;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.RecipeData;
import com.mastermarisa.maid_restaurant.network.CookOrderPayload;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class UIConfirmButton extends UIButton {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/confirm.png");
    private final OrderingScreen screen;

    public UIConfirmButton(OrderingScreen screen) {
        super(new Rectangle(11,7),(button) -> {
            ((UIConfirmButton) button).sendOrder();
            return true;
        });
        this.screen = screen;
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.render(graphics, mouseX, mouseY);
        if (!screen.orders.isEmpty()) {
            graphics.blit(texture, getMinX(), getMinY(), 0,0,11,7,11,7);
        }
    }

    public void sendOrder() {
        MaidRestaurant.LOGGER.debug("SEND ORDER");
        List<OrderingScreen.Order> orders = screen.orders;
        String[] recipeIDs = new String[orders.size()];
        String[] recipeTypes = new String[orders.size()];
        int[] counts = new int[orders.size()];
        long[] tables = new long[screen.targetTable.size()];

        for (int i = 0;i < orders.size();i++) {
            OrderingScreen.Order order = orders.get(i);
            recipeIDs[i] = order.data.ID.toString();
            recipeTypes[i] = CookTaskManager.getUID(order.data.type);
            counts[i] = order.count;
        }

        for (int i = 0;i < screen.targetTable.size();i++) {
            tables[i] = screen.targetTable.get(i).asLong();
        }

        CookOrderPayload payload = new CookOrderPayload(recipeIDs,recipeTypes,counts,tables);
        PacketDistributor.sendToServer(payload);

        screen.close();
    }
}
