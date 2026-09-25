package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.inventory.container.WaiterLicenseContainer;
import com.mastermarisa.maid_restaurant.item.WaiterLicenseItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.RestaurantIdUpdateMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WaiterLicenseScreen extends AbstractContainerScreen<WaiterLicenseContainer> {
    private EditBox chefIdField;

    public WaiterLicenseScreen(WaiterLicenseContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        int fieldX = this.leftPos + 43;
        int fieldY = this.topPos + 18;
        this.chefIdField = new EditBox(
                this.font,
                fieldX, fieldY,
                90, 16,
                Component.literal("")
        );
        this.chefIdField.setMaxLength(50);
//        this.chefIdField.setBordered(false);
        this.chefIdField.setEditable(true);
        this.chefIdField.setCanLoseFocus(true);
        String existingId = WaiterLicenseItem.getRestaurantId(this.menu.getItemStack());
        if (!existingId.isEmpty()) {
            this.chefIdField.setValue(existingId);
        }
        this.addRenderableWidget(this.chefIdField);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pt) {
        super.render(graphics, mouseX, mouseY, pt);
        this.chefIdField.render(graphics, mouseX, mouseY, pt);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        renderBackground(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void resize(Minecraft minecraft, int pWidth, int pHeight) {
        String text = this.chefIdField != null ? this.chefIdField.getValue() : "";
        super.resize(minecraft, pWidth, pHeight);
        this.chefIdField.setValue(text);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.chefIdField.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_ESCAPE) {
            this.chefIdField.setFocused(false);
            String text = this.chefIdField.getValue().trim();
            if (!text.isEmpty()) {
                NetworkHandler.sendToServer(new RestaurantIdUpdateMessage(text));
            }
            return true;
        }

        return this.chefIdField.keyPressed(keyCode, scanCode, modifiers);
    }
}
