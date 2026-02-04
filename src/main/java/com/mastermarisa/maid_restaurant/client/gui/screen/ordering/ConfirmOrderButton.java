package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.network.CookOrderPayload;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConfirmOrderButton extends Button {
    private static final ResourceLocation texture = MaidRestaurant.resourceLocation("textures/gui/confirm.png");
    private final OrderingScreen screen;

    public ConfirmOrderButton(int x,int y,OrderingScreen screen){
        super(x, y, 11, 7, CommonComponents.EMPTY,(button) -> ((ConfirmOrderButton)button).sendOrder(), DEFAULT_NARRATION);
        this.screen = screen;
    }

    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            graphics.blit(texture, this.getX(), this.getY(), 0,0,11,7,11,7);
        }
    }

    public void updateState() {
        this.visible = !screen.orders.isEmpty();
    }

    public void sendOrder() {
        MaidRestaurant.LOGGER.debug("SEND ORDER");
        List<Pair<RecipeData,Integer>> orders = screen.orders;
        String[] recipeIDs = new String[orders.size()];
        String[] recipeTypes = new String[orders.size()];
        int[] counts = new int[orders.size()];
        long[] tables = new long[screen.targetTable.size()];

        for (int i = 0;i < orders.size();i++) {
            Pair<RecipeData,Integer> order = orders.get(i);
            recipeIDs[i] = order.left().ID.toString();
            recipeTypes[i] = CookTaskManager.getUID(order.left().type);
            counts[i] = order.right();
        }

        for (int i = 0;i < screen.targetTable.size();i++) {
            tables[i] = screen.targetTable.get(i).asLong();
        }

        CookOrderPayload payload = new CookOrderPayload(recipeIDs,recipeTypes,counts,tables);
        PacketDistributor.sendToServer(payload);

        screen.close();
    }
}
