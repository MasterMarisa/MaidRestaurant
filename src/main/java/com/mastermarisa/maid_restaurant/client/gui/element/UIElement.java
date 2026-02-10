package com.mastermarisa.maid_restaurant.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class UIElement {
    protected static final Minecraft mc = Minecraft.getInstance();
    protected static final Font font;
    public Rectangle frame;
    @Nullable
    protected java.util.List<? extends FormattedText> tooltip = new ArrayList<>();
    @Nullable
    protected UIElement father;
    protected List<UIElement> children = new ArrayList<>();

    static {
        font = mc.font;
    }

    public UIElement(Rectangle frame) {
        this.frame = frame;
    }

    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        this.children.forEach((child) -> child.render(graphics,mouseX,mouseY));
    }

    public static void render(GuiGraphics graphics, UIElement element, int mouseX, int mouseY) {
        render(graphics, Collections.singletonList(element), mouseX, mouseY);
    }

    public static void render(GuiGraphics graphics, List<? extends UIElement> elements, int mouseX, int mouseY) {
        elements.forEach((element) -> element.render(graphics,mouseX,mouseY));
        elements.stream().flatMap(UIElement::getRecursiveChildren).forEach((element)-> element.tryRenderTooltip(graphics, mouseX, mouseY));
    }

    public static void renderToolTip(GuiGraphics graphics, UIElement element, int mouseX, int mouseY) {
        element.getRecursiveChildren().forEach((e) -> e.tryRenderTooltip(graphics, mouseX, mouseY));
    }

    public static boolean onMouseClicked(UIElement element, double mouseX, double mouseY, int button) {
        return onMouseClicked(List.of(element),mouseX,mouseY,button);
    }

    public static boolean onMouseClicked(List<? extends UIElement> elements, double mouseX, double mouseY, int button) {
        return elements.stream().anyMatch(e -> e.onMouseClicked(mouseX,mouseY,button));
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.children.stream().anyMatch(c -> c.onMouseScrolled(mouseX,mouseY,scrollX,scrollY));
    }

    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return this.children.stream().anyMatch(c -> c.onMouseClicked(mouseX,mouseY,button));
    }

    protected void tryRenderTooltip(GuiGraphics graphics, int mouseX, int mouseY){
        if (!hasTooltip()) return;
        boolean hover = frame.contains(mouseX,mouseY);
        if (hover){
            renderTooltip(graphics, ItemStack.EMPTY,tooltip,mouseX,mouseY);
        }
    }

    protected void tryRenderTooltip(GuiGraphics graphics, ItemStack itemStack, int mouseX, int mouseY){
        if (!hasTooltip()) return;
        boolean hover = frame.contains(mouseX,mouseY);
        if (hover){
            renderTooltip(graphics,itemStack,tooltip,mouseX,mouseY);
        }
    }

    protected final void renderTooltip(GuiGraphics graphics, ItemStack itemStack, List<? extends FormattedText> tooltip, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY, itemStack);
    }

    public void addChild(UIElement element){
        element.father = this;
        children.add(element);
    }

    public void removeChild(UIElement element){
        element.father = null;
        children.remove(element);
    }

    protected Stream<UIElement> getRecursiveChildren() {
        return Stream.concat(Stream.of(this), this.children.stream().flatMap(UIElement::getRecursiveChildren));
    }

    public boolean hasTooltip(){ return this.tooltip != null && !this.tooltip.isEmpty();}

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

    public static <T extends UIElement> UIElement toUIElement(T element){
        return (UIElement) element;
    }
}
