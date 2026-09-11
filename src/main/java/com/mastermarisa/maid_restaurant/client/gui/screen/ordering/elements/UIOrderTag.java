package com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements;

import com.mastermarisa.maid_restaurant.client.gui.RecipeMaterials;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.base.UIImage;
import com.mastermarisa.maid_restaurant.client.gui.base.UIItemStackWithCount;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.OrderingScreen;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIOrderTag extends UIElement {
    /** Tooltip lines shown before the list gets truncated. */
    private static final int MAX_WARNING_LINES = 6;

    /** Ticks between two material scans; the scan itself walks every nearby block. */
    private static final int REFRESH_TICKS = 10;

    private final UIImage bg;
    private final UIItemStackWithCount result;
    private final UICancelButton btn;
    protected OrderingScreen screen;
    protected int index;

    /** Ingredients this order still needs and the player cannot supply, refreshed periodically. */
    private List<ItemStack> missing = List.of();
    private long lastRefreshTick = Long.MIN_VALUE;

    public UIOrderTag(RecipeData data, int count, OrderingScreen screen, int index) {
        super(new Rectangle(58,18));
        bg = new UIImage(UIConst.orderedTagImage);
        result = new UIItemStackWithCount(data.result.copyWithCount(data.result.getCount() * count),UIConst.lessBlack);
        result.dropShadow = false;
        this.screen = screen;
        this.index = index;
        btn = new UICancelButton(index,screen);

        children = List.of(bg,result,btn);
        refreshMissing();
    }

    @Override
    protected void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        refreshMissing();
        super.render(graphics,mouseX,mouseY);
        resize();
    }

    public void resize() {
        bg.setCenterX(getCenterX());
        bg.setCenterY(getCenterY());
        result.setMaxX(bg.getMaxX() - 21);
        result.setCenterY(getCenterY());
        btn.setMinX(getMinX() + 5);
        btn.setCenterY(getCenterY());
    }

    /** The ingredients this order is short of, as deficit-carrying stacks. Empty means "can cook". */
    public List<ItemStack> getMissing() {
        return missing;
    }

    public boolean hasMissing() {
        return !missing.isEmpty();
    }

    /**
     * Re-scans at most every {@link #REFRESH_TICKS} ticks. {@link RecipeMaterials} caches the block
     * scan itself, so several tags refreshing on the same tick share one pass over the world.
     */
    private void refreshMissing() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || screen.orders.size() <= index) return;

        long now = minecraft.level.getGameTime();
        if (now - lastRefreshTick < REFRESH_TICKS) return;
        lastRefreshTick = now;

        // One extra entry beyond what the tooltip prints, so the "and N more" tail has something to
        // report instead of the list silently ending at the cap.
        missing = RecipeMaterials.missingOf(List.of(screen.orders.get(index)), MAX_WARNING_LINES + 1);
        syncWarningState();
    }

    private void syncWarningState() {
        result.setColor(missing.isEmpty() ? UIConst.lessBlack : UIConst.warning);
        this.tooltip = new ArrayList<>(RecipeMaterials.describe(missing, MAX_WARNING_LINES));
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (bg.frame.contains(mouseX,mouseY)) {
            if (scrollY > 0) {
                OrderingScreen.Order order = screen.orders.get(index);
                RecipeData data = order.data;
                if (data.result.getCount() * (order.count + 1) <= data.result.getMaxStackSize()) {
                    order.count++;
                    result.setCount(data.result.getCount() * order.count);
                    lastRefreshTick = Long.MIN_VALUE;
                }
                return true;
            } else if (scrollY < 0) {
                OrderingScreen.Order order = screen.orders.get(index);
                RecipeData data = order.data;
                if (order.count > 1) {
                    order.count--;
                    result.setCount(data.result.getCount() * order.count);
                    lastRefreshTick = Long.MIN_VALUE;
                }
                return true;
            }
        }

        return false;
    }
}
