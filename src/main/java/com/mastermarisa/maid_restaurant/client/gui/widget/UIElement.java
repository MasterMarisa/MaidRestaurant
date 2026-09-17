package com.mastermarisa.maid_restaurant.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class UIElement {
    protected static final Minecraft mc;
    protected static final Font font;

    public Rectangle frame;
    public List<Component> tooltip = new ArrayList<>();
    protected List<UIElement> children = new ArrayList<>();

    public UIElement(Rectangle frame) {
        this.frame = frame;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        this.children.forEach((child) -> child.render(graphics, mouseX, mouseY));
    }

    public void tryRenderTooltip(GuiGraphics graphics, int mouseX, int mouseY){
        tryRenderTooltip(graphics, ItemStack.EMPTY, mouseX, mouseY);
    }

    public void tryRenderTooltip(GuiGraphics graphics, ItemStack itemStack, int mouseX, int mouseY){
        boolean hover = hasTooltip() && frame.contains(mouseX,mouseY);
        if (hover){
            renderTooltip(graphics, itemStack, tooltip, mouseX, mouseY);
        }
    }

    public final void renderTooltip(GuiGraphics graphics, ItemStack itemStack, List<? extends FormattedText> tooltip, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY, itemStack);
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.children.stream().anyMatch(c -> c.onMouseScrolled(mouseX,mouseY,scrollX,scrollY));
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return this.children.stream().anyMatch(c -> c.onMouseClicked(mouseX,mouseY,button));
    }

    public Rectangle getFrame() { return this.frame; }

    public void addChild(UIElement element){
        children.add(element);
    }

    public void removeChild(UIElement element){
        children.remove(element);
    }

    public void clearChildren() { children.clear(); }

    public boolean hasTooltip(){ return !this.tooltip.isEmpty();}

    public final int getCenterX() {
        return this.frame.x + this.frame.width / 2;
    }

    public final void setCenterX(int centerX) {
        this.frame.setLocation(centerX - this.frame.width / 2, this.frame.y);
    }

    public final int getCenterY() {
        return this.frame.y + this.frame.height / 2;
    }

    public final void setCenterY(int centerY) {
        this.frame.setLocation(this.frame.x, centerY - this.frame.height / 2);
    }

    public final void setCenter(int centerX, int centerY) {
        this.setCenterX(centerX);
        this.setCenterY(centerY);
    }

    public final int getMinX() {
        return this.frame.x;
    }

    public final void setMinX(int minX) {
        this.frame.setLocation(minX, this.frame.y);
    }

    public final int getMinY() {
        return this.frame.y;
    }

    public final void setMinY(int minY) {
        this.frame.setLocation(this.frame.x, minY);
    }

    public final int getMaxX() {
        return this.frame.x + this.frame.width;
    }

    public final void setMaxX(int maxX) {
        this.frame.setLocation(maxX - this.frame.width, this.frame.y);
    }

    public final int getMaxY() {
        return this.frame.y + this.frame.height;
    }

    public final void setMaxY(int maxY) {
        this.frame.setLocation(this.frame.x, maxY - this.frame.height);
    }

    public final int getWidth() {
        return this.frame.width;
    }

    public final void setWidth(int width) {
        this.frame.width = width;
    }

    public final int getHeight() {
        return this.frame.height;
    }

    public final void setHeight(int height) {
        this.frame.height = height;
    }

    public final void setSize(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }
}
