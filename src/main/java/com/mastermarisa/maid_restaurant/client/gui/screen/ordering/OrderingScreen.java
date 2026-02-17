package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.gui.IPageable;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.UIContainerVertical;
import com.mastermarisa.maid_restaurant.client.gui.base.UIEditBox;
import com.mastermarisa.maid_restaurant.client.gui.base.UIElement;
import com.mastermarisa.maid_restaurant.client.gui.base.UILabel;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.RecipePage;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UIConfirmTag;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UICookTypeSelector;
import com.mastermarisa.maid_restaurant.client.gui.screen.ordering.elements.UIOrderTag;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class OrderingScreen extends Screen implements IPageable {
    private static final Minecraft mc;
    private static final Font font;

    public final Player player;
    public final List<BlockPos> targets;
    public List<Order> orders;
    public RecipeType<?> curType;
    private int curPageNum;

    private final ConcurrentHashMap<RecipeType<?>,List<RecipePage>> pageMap;

    private final UIEditBox searchBox;
    private UIContainerVertical orderTags;
    private UILabel pageNumLabel;
    private final UICookTypeSelector selector;

    public OrderingScreen(Player player, List<BlockPos> targets) {
        super(Component.empty());
        this.player = player;
        this.targets = targets;
        this.pageMap = new ConcurrentHashMap<>();
        this.orders = new ArrayList<>();

        this.curType = CookTasks.getRegistered().get(0).getType();
        initPages("");
        initOrderTags();

        this.searchBox = new UIEditBox(
                new Rectangle(50,14),
                new Color(0,0,0,0.2f),
                25,
                this::onSearchTextChanged,
                UIConst.lessBlack
        );
        this.searchBox.tooltip.add(Component.literal("搜索").withStyle(ChatFormatting.GOLD));

        RecipePage page = getCurrentPage();
        resizeSearchBox(page.getCenterX(),page.getMinY() + 21);

        selector = new UICookTypeSelector(this);
        selector.setCenterX(getScreenCenterX());
        selector.setMinY(page.getMaxY() + 5);
        selector.resize();

        refreshPageNum();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        UIElement.render(graphics,getCurrentPage(),mouseX,mouseY);
        UIElement.render(graphics,searchBox,mouseX,mouseY);
        UIElement.render(graphics,orderTags,mouseX,mouseY);
        UIElement.render(graphics,selector,mouseX,mouseY);
        UIElement.render(graphics,pageNumLabel,mouseX,mouseY);
        UIElement.renderToolTip(graphics,getCurrentPage(),mouseX,mouseY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        RecipePage page = getCurrentPage();
        page.setCenter(getScreenCenterX(),getScreenCenterY());
        page.onResize();
        resizeSearchBox(page.getCenterX(),page.getMinY() + 21);
        initOrderTags();
        selector.setCenterX(getScreenCenterX());
        selector.setMinY(page.getMaxY() + 5);
        selector.resize();
        refreshPageNum();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scrolled = UIElement.onMouseScrolled(orderTags,mouseX,mouseY,scrollX,scrollY) || selector.onMouseScrolled(mouseX,mouseY,scrollX,scrollY);
        RecipePage page = getCurrentPage();
        if (!scrolled && !UIElement.onMouseScrolled(page,mouseX,mouseY,scrollX,scrollY)) {
            if (scrollY > 0) switchToPage(curPageNum - 1);
            else if (scrollY < 0) switchToPage(curPageNum + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getCurrentPage() != null) UIElement.onMouseClicked(getCurrentPage(),mouseX,mouseY,button);
        UIElement.onMouseClicked(orderTags,mouseX,mouseY,button);
        UIElement.onMouseClicked(selector,mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void resizeSearchBox(int centerX, int centerY) {
        searchBox.setCenter(centerX,centerY);
        searchBox.resize();
        this.removeWidget(searchBox.getEditBox());
        this.addRenderableWidget(searchBox.getEditBox());
    }

    private void onSearchTextChanged(String text) {
        initPages(text);
    }

    public static void open(Player player, List<BlockPos> targets) {
        mc.setScreen(new OrderingScreen(player,targets));
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

    @Override
    public void switchToPage(int pageNumber) {
        if (isWithinRange(pageNumber)) {
            curPageNum = pageNumber;
            RecipePage page = getCurrentPage();
            page.setCenter(getScreenCenterX(),getScreenCenterY());
            page.onSwitchedTo();
            refreshPageNum();
        }
    }

    @Override
    public int getCurrentPageNumber() {
        return curPageNum;
    }

    @Override
    public boolean isWithinRange(int pageNumber) {
        return 0 <= pageNumber && pageNumber < pageMap.getOrDefault(curType,new ArrayList<>()).size();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public RecipePage getCurrentPage() {
        List<RecipePage> pages = pageMap.getOrDefault(curType,new ArrayList<>());
        return pages.size() <= curPageNum ? null : pages.get(curPageNum);
    }

    public void order(int index) {
        if (getCurrentPage() == null) return;
        RecipeData data = getCurrentPage().data.get(index);
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

    public void initPages(String filter) {
        for (ICookTask iCookTask : CookTasks.getRegistered()) {
            List<RecipeData> data = filter(iCookTask.getAllRecipeData(player.level()),filter);
            List<RecipePage> recipePages = new ArrayList<>();
            for (int i = 0;i < data.size();i += 14) {
                List<RecipeData> pageData = new ArrayList<>();
                for (int j = i;j < i + 14;j++)
                    if (j < data.size()) pageData.add(data.get(j));
                recipePages.add(new RecipePage(pageData,this));
            }
            if (recipePages.isEmpty()) {
                recipePages.add(new RecipePage(new ArrayList<>(),this));
            }
            pageMap.put(iCookTask.getType(),recipePages);
        }

        if (pageMap.isEmpty()) close();
        else {
            switchToPage(0);
        }
    }

    public void initOrderTags() {
        RecipePage page = getCurrentPage();
        List<UIOrderTag> tags = new ArrayList<>();
        for (int i = 0;i < orders.size();i++) {
            Order order = orders.get(i);
            tags.add(new UIOrderTag(order.data,order.count,this,i));
        }

        orderTags = UIContainerVertical.wrap(tags,2,0, UIContainerVertical.ElementAlignment.UP);
        orderTags.setMinY(page.getMinY() + 10);
        orderTags.setMaxX(page.getMinX() + 15);

        UIConfirmTag confirmTag = new UIConfirmTag(this);
        if (!orders.isEmpty()) orderTags.addChild(confirmTag);

        orderTags.order();
        tags.forEach(UIOrderTag::resize);
        if (!orders.isEmpty()) confirmTag.resize();
    }

    public void refreshPageNum() {
        RecipePage page = getCurrentPage();
        pageNumLabel = new UILabel(
                (curPageNum + 1) + "/" + pageMap.getOrDefault(curType,new ArrayList<>()).size(),
                UILabel.TextAlignment.CENTER,
                UIConst.lessBlack,
                false
        );
        pageNumLabel.setCenterX(page.getCenterX());
        pageNumLabel.setMaxY(page.getMaxY() - 10);
    }

    public List<RecipeData> filter(List<RecipeData> input, String filter) {
        if (filter == null || filter.isEmpty()) return input;
        List<RecipeData> filtered = new ArrayList<>();
        filter = filter.toLowerCase(Locale.ROOT);
        for (RecipeData data : input) {
            String displayName = data.result.getHoverName().getString().toLowerCase(Locale.ROOT);
            String registerName = data.result.getDescriptionId();
            if (displayName.contains(filter))
                filtered.add(data);
            else if (registerName.contains(filter))
                filtered.add(data);
        }
        return filtered;
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
