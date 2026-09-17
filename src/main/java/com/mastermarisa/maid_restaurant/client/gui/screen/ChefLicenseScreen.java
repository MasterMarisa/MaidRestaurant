package com.mastermarisa.maid_restaurant.client.gui.screen;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.inventory.container.ChefLicenseContainer;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.network.NetworkHandler;
import com.mastermarisa.maid_restaurant.network.message.RestaurantIdUpdateMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChefLicenseScreen extends AbstractContainerScreen<ChefLicenseContainer> {
    private static final ResourceLocation backgroundImage = MaidRestaurant.modLoc("textures/gui/chef_license.png");
    private static final Component workZoneLabel = Component.literal("工作区").withStyle(ChatFormatting.BOLD);
    private static final Component storageZoneLabel = Component.literal("储存区").withStyle(ChatFormatting.BOLD);
    private static final Component prepZoneLabel = Component.literal("备餐区").withStyle(ChatFormatting.BOLD);

    private EditBox chefIdField;

    public ChefLicenseScreen(ChefLicenseContainer container, Inventory inventory, Component title) {
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
        this.chefIdField.setBordered(false);
        this.chefIdField.setEditable(true);
        this.chefIdField.setCanLoseFocus(false);
        String existingId = ChefLicenseItem.getRestaurantId(this.menu.getLicense());
        if (!existingId.isEmpty()) {
            this.chefIdField.setValue(existingId);
        }
        this.addRenderableWidget(this.chefIdField);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float v, int i, int i1) {
        renderBackground(graphics);
        graphics.blit(backgroundImage, this.leftPos, this.topPos, 0, 0, 176, 190, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pt) {
        super.render(graphics, mouseX, mouseY, pt);
        this.chefIdField.render(graphics, mouseX, mouseY, pt);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.chefIdField.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == InputConstants.KEY_RETURN) {
            this.chefIdField.setCanLoseFocus(true);
            this.chefIdField.setFocused(false);
            this.chefIdField.setCanLoseFocus(false);
            String text = this.chefIdField.getValue().trim();
            if (!text.isEmpty()) {
                NetworkHandler.sendToServer(new RestaurantIdUpdateMessage(text));
            }
            return true;
        }

        return this.chefIdField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String text = this.chefIdField != null ? this.chefIdField.getValue() : "";
        super.resize(pMinecraft, pWidth, pHeight);
        this.chefIdField.setValue(text);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int width = font.width(workZoneLabel);
        graphics.drawString(font, workZoneLabel, 43 - width / 2, 42, 14737632, false);
        width = font.width(storageZoneLabel);
        graphics.drawString(font, storageZoneLabel, 88 - width / 2, 42, 14737632, false);
        width = font.width(prepZoneLabel);
        graphics.drawString(font, prepZoneLabel, 133 - width / 2, 42, 14737632, false);
    }
}
