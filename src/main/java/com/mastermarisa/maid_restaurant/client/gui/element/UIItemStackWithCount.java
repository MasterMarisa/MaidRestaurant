package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.awt.*;
import java.util.List;

public class UIItemStackWithCount extends UIElement {
    private final ItemStack itemStack;
    private final Color color;
    public boolean dropShadow = true;

    public UIItemStackWithCount(ItemStack itemStack, Color color) {
        super(new Rectangle(21, 16));
        this.itemStack = itemStack;
        this.color = color;
    }

    public UIItemStackWithCount(ItemStack itemStack) {
        super(new Rectangle(21, 16));
        this.itemStack = itemStack;
        this.color = Color.WHITE;
    }

    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        graphics.renderItem(this.itemStack, this.frame.x, this.frame.y + (this.frame.height - 16) / 2);
        graphics.drawString(font,"x" + itemStack.getCount(),getCenterX() + 5,getMaxY() - font.lineHeight, color.getRGB(),dropShadow);
    }

    public boolean hasTooltip() {
        return true;
    }

    protected void tryRenderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!hasTooltip()) return;
        boolean hover = frame.contains(mouseX,mouseY);
        if (hover){
            if (tooltip != null && !tooltip.isEmpty()) {
                this.renderTooltip(graphics, this.itemStack, tooltip, mouseX, mouseY);
            } else {
                List<Component> tooltip = this.itemStack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                this.renderTooltip(graphics, this.itemStack, tooltip, mouseX, mouseY);
            }
        }
    }
}
