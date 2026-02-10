package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.gui.IPageable;
import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UIConfirmTag;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UIOrderTag;
import com.mastermarisa.maid_restaurant.init.UIConst;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class OrderingScreen extends Screen implements IPageable {
    private static final Minecraft mc;
    private static final Font font;

    public final Player player;
    public final List<BlockPos> targetTable;

    private final ConcurrentHashMap<RecipeType<?>,List<ShowRecipePage>> pages;
    private UIContainerVertical orderTags;
    private final UICookTypeSelector selector;
    private UILabel pageNumLabel;

    public List<Order> orders;
    private int curPageNum;
    public RecipeType<?> curType;

    public OrderingScreen(Player player, List<BlockPos> targetTable) {
        super(Component.empty());
        CookTaskManager.register();
        this.player = player;
        this.targetTable = targetTable;
        this.pages = new ConcurrentHashMap<>();
        this.orders = new ArrayList<>();
        initPages();
        initOrderTags();
        selector = new UICookTypeSelector(this);
        selector.setCenterX(getScreenCenterX());
        selector.setMinY(getCurPage().getMaxY() + 5);
        selector.resize();
        refreshPageNum();
    }

    public void initPages() {
        for (RecipeType<?> type : CookTaskManager.getAllRegisteredTypes()) {
            ICookTask iCookTask = CookTaskManager.getTask(type).get();
            List<RecipeData> data = iCookTask.getAllRecipeData();
            List<ShowRecipePage> recipePages = new ArrayList<>();
            for (int i = 0;i < data.size();i += 16) {
                List<RecipeData> pageData = new ArrayList<>();
                for (int j = i;j < i + 16;j++)
                    if (j < data.size()) pageData.add(data.get(j));
                recipePages.add(new ShowRecipePage(pageData,this));
            }
            pages.put(type,recipePages);
        }

        if (pages.isEmpty()) close();
        else {
            curType = CookTaskManager.getAllRegisteredTypes().get(0);
            switchToPage(0);
        }
    }

    public void initOrderTags() {
        if (getCurPage() == null) return;
        List<UIOrderTag> tags = new ArrayList<>();
        for (int i = 0;i < orders.size();i++) {
            Order order = orders.get(i);
            tags.add(new UIOrderTag(order.data,order.count,this,i));
        }

        orderTags = UIContainerVertical.wrap(tags,2,0, UIContainerVertical.ElementAlignment.UP);
        orderTags.setMinY(getCurPage().getMinY() + 10);
        orderTags.setMaxX(getCurPage().getMinX() + 15);

        UIConfirmTag confirmTag = new UIConfirmTag(this);
        if (!orders.isEmpty()) orderTags.addChild(confirmTag);

        orderTags.order();
        tags.forEach(UIOrderTag::resize);
        if (!orders.isEmpty()) confirmTag.resize();
    }

    public void refreshPageNum() {
        pageNumLabel = new UILabel((curPageNum + 1) + "/" + getCurPages(curType).size(), UIConst.lessBlack);
        pageNumLabel.setCenterX(getCurPage().getCenterX());
        pageNumLabel.setMaxY(getCurPage().getMaxY() - 10);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics,mouseX,mouseY,partialTicks);
        if (getCurPage() == null) return;
        UIElement.render(graphics,getCurPage(),mouseX,mouseY);
        UIElement.render(graphics,orderTags,mouseX,mouseY);
        UIElement.render(graphics,selector,mouseX,mouseY);
        UIElement.render(graphics,pageNumLabel,mouseX,mouseY);
        UIElement.renderToolTip(graphics,getCurPage(),mouseX,mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scrolled = orderTags.onMouseScrolled(mouseX,mouseY,scrollX,scrollY) || selector.onMouseScrolled(mouseX,mouseY,scrollX,scrollY);
        if (getCurPage() != null && !scrolled && !getCurPage().onMouseScrolled(mouseX,mouseY,scrollX,scrollY)) {
            if (scrollY > 0) switchToPage(curPageNum - 1);
            else if (scrollY < 0) switchToPage(curPageNum + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (getCurPage() == null) return;
        getCurPage().setCenterX(getScreenCenterX());
        getCurPage().setCenterY(getScreenCenterY());
        getCurPage().onResize();
        orderTags.setMinY(getCurPage().getMinY());
        orderTags.setMaxX(getCurPage().getMinX() + 5);
        selector.setCenterX(getScreenCenterX());
        selector.setMinY(getCurPage().getMaxY() + 5);
        selector.resize();
        refreshPageNum();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getCurPage() != null) UIElement.onMouseClicked(getCurPage(),mouseX,mouseY,button);
        UIElement.onMouseClicked(orderTags,mouseX,mouseY,button);
        UIElement.onMouseClicked(selector,mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void switchToPage(int pageNumber) {
        if (isWithinRange(pageNumber)) {
            curPageNum = pageNumber;
            if (getCurPage() == null) return;
            getCurPage().setCenterX(getScreenCenterX());
            getCurPage().setCenterY(getScreenCenterY());
            getCurPage().onSwitchedTo();
            refreshPageNum();
        }
    }

    @Override
    public int getCurrentPageNumber() {
        return curPageNum;
    }

    @Override
    public boolean isWithinRange(int pageNumber) {
        return 0 <= pageNumber && pageNumber < getCurPages(curType).size();
    }

    public List<ShowRecipePage> getCurPages(RecipeType<?> type) { return pages.getOrDefault(type,new ArrayList<>()); }

    public @Nullable ShowRecipePage getCurPage() { return getCurPages(curType).size() <= curPageNum ? null : getCurPages(curType).get(curPageNum); }

    public void order(int index) {
        if (getCurPage() == null) return;
        RecipeData data = getCurPage().data.get(index);
        boolean added = false;

        for (Order order : orders) {
            RecipeData exist = order.data;
            if (exist.ID.equals(data.ID) && exist.result.getCount() * (order.count + 1) <= exist.result.getMaxStackSize()) {
                order.count++;
                added = true;
                initOrderTags();
                break;
            }
        }

        if (!added && orders.size() < 7) {
            orders.add(new Order(data,1));
            initOrderTags();
        }
    }

    public void cancel(int index) {
        if (orders.size() > index) {
            orders.remove(index);
            initOrderTags();
        }
    }

    public static void open(Player player, List<BlockPos> targetTable) {
        mc.setScreen(new OrderingScreen(player,targetTable));
    }

    public void close() {
        mc.setScreen(null);
    }

    public static int getScreenCenterX(){
        return mc.getWindow().getGuiScaledWidth() / 2;
    }

    public static int getScreenCenterY(){
        return mc.getWindow().getGuiScaledHeight() / 2;
    }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }

    public static class Order {
        public RecipeData data;
        public int count;

        public Order(RecipeData data, int count) {
            this.data = data;
            this.count = count;
        }
    }
}
