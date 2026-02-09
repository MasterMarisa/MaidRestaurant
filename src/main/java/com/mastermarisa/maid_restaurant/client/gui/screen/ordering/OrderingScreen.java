package com.mastermarisa.maid_restaurant.client.gui.screen.ordering;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.api.gui.IPageable;
import com.mastermarisa.maid_restaurant.client.gui.element.*;
import com.mastermarisa.maid_restaurant.init.UIConst;
import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import it.unimi.dsi.fastutil.Pair;
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

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OrderingScreen extends Screen implements IPageable {
    private static final Minecraft mc;
    private static final Font font;

    protected final Player player;
    public final List<BlockPos> targetTable;

    public List<Pair<RecipeData,Integer>> orders;
    private int currentPageNumber;

    protected final List<ShowRecipePage> pages;
    private UIContainerVertical orderTags;
    private UILabel pageNum;

    private List<OrderButton> orderButtons;
    private List<CancelOrderButton> cancelButtons;
    private ConfirmOrderButton confirmButton;

    public OrderingScreen(Player player, List<BlockPos> targetTable) {
        super(Component.empty());
        CookTaskManager.register();
        this.player = player;
        this.targetTable = targetTable;
        this.pages = new ArrayList<>();
        this.orderButtons = new ArrayList<>();
        this.cancelButtons = new ArrayList<>();
        this.orders = new ArrayList<>();
        initPages();
        initButtons();
        initOrderTags();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics,mouseX,mouseY,partialTicks);
        if (!pages.isEmpty()) {
            UIElement.render(graphics,pages.get(currentPageNumber),mouseX,mouseY);
            for (var btn : orderButtons) {
                btn.render(graphics,mouseX,mouseY,partialTicks);
            }
            UIElement.render(graphics,orderTags,mouseX,mouseY);
            for (var btn : cancelButtons) {
                btn.render(graphics,mouseX,mouseY,partialTicks);
            }
            confirmButton.render(graphics,mouseX,mouseY,partialTicks);
            UIElement.render(graphics,pageNum,mouseX,mouseY);
            UIElement.renderToolTip(graphics,pages.get(currentPageNumber),mouseX,mouseY);
            UIElement.renderToolTip(graphics,orderTags,mouseX,mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!pages.isEmpty()) {
            if (!pages.get(currentPageNumber).onMouseScrolled(mouseX,mouseY,scrollX,scrollY)) {
                if (scrollY > 0) {
                    switchToPage(currentPageNumber - 1);
                } else if (scrollY < 0) {
                    switchToPage(currentPageNumber + 1);
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (!pages.isEmpty()) {
            pages.get(currentPageNumber).setCenterX(getScreenCenterX());
            pages.get(currentPageNumber).setCenterY(getScreenCenterY());
            pages.get(currentPageNumber).onResize();
            orderTags.setMinY(getCurrentPage().getMinY());
            orderTags.setMaxX(getCurrentPage().getMinX() + 5);
            pageNum.setCenterX(getCurrentPage().getCenterX());
            pageNum.setMaxY(getCurrentPage().getMaxY() - 10);
        }
        initButtons();
        initOrderTags();
    }

    public static void open(Player player,List<BlockPos> targetTable) {
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

    public void initPages() {
        List<RecipeData> data = new ArrayList<>();
        for (RecipeType<?> type : CookTaskManager.getAllRegisteredTypes()) {
            ICookTask iCookTask = CookTaskManager.getTask(type).get();
            data.addAll(iCookTask.getAllRecipeData());
        }

        for (int i = 0;i < data.size();i += 8) {
            List<RecipeData> pageData = new ArrayList<>();
            for (int j = i;j < i + 8;j++) {
                if (j < data.size()) {
                    pageData.add(data.get(j));
                }
            }
            pages.add(new ShowRecipePage(pageData));
        }

        pageNum = new UILabel("1" + "/" + pages.size(),UIConst.lessBlack);
        pageNum.setCenterX(getCurrentPage().getCenterX());
        pageNum.setMaxY(getCurrentPage().getMaxY() - 10);
        if (pages.isEmpty()) {
            close();
            return;
        }

        switchToPage(0);
    }

    public void initOrderTags() {
        List<UIElement> tags = new ArrayList<>();
        for (int i = 0;i < orders.size();i++) {
            var pair = orders.get(i);
            tags.add(new UIOrderTag(pair.left(),pair.right(),this,i));
        }

        orderTags = UIContainerVertical.wrap(tags,2,0, UIContainerVertical.ElementAlignment.UP);
        orderTags.setMinY(getCurrentPage().getMinY() + 10);
        orderTags.setMaxX(getCurrentPage().getMinX() + 15);

        if (!orders.isEmpty()) {
            UIImage confirmTag = new UIImage(UIConst.confirmTagImage);
            UIContainerHorizontal horizontal = UIContainerHorizontal.wrap(new ArrayList<>(List.of(confirmTag)),0,0, UIContainerHorizontal.ElementAlignment.RIGHT);
            horizontal.setWidth(58);
            orderTags.addChild(horizontal);
        }

        orderTags.order();

        for (var tag : tags) {
            ((UIOrderTag)tag).resize();
        }

        updateButtonVisibility();
    }

    public void initButtons() {
        for (var btn : orderButtons)
            this.removeWidget(btn);

        for (var btn : cancelButtons)
            this.removeWidget(btn);

        ShowRecipePage page = getCurrentPage();
        orderButtons = new ArrayList<>();
        cancelButtons = new ArrayList<>();
        for (int i = 0;i < 8;i++) {
            orderButtons.add(this.addRenderableWidget(new OrderButton(page.getMaxX() - 31,page.getMinY() + 11 + 19 * i,i,this)));
            cancelButtons.add(this.addRenderableWidget(new CancelOrderButton(page.getMinX() - 38,page.getMinY() + 16 + 20 * i,i,this)));
        }
        updateButtonVisibility();
    }

    public void updateButtonVisibility(){
        for (var btn : orderButtons)
            btn.updateState();
        for (var btn : cancelButtons)
            btn.updateState();
        if (confirmButton != null) this.removeWidget(confirmButton);
        confirmButton = this.addRenderableWidget(new ConfirmOrderButton(getCurrentPage().getMinX() - 6,getCurrentPage().getMinY() + 16 + 20 * orders.size(),this));
        confirmButton.updateState();
    }

    public void switchToPage(int pageNumber) {
        if (isWithinRange(pageNumber)) {
            currentPageNumber = pageNumber;
            pages.get(currentPageNumber).setCenterX(getScreenCenterX());
            pages.get(currentPageNumber).setCenterY(getScreenCenterY());
            pages.get(currentPageNumber).onSwitchedTo();
            pageNum = new UILabel((pageNumber + 1) + "/" + pages.size(),UIConst.lessBlack);
            pageNum.setCenterX(getCurrentPage().getCenterX());
            pageNum.setMaxY(getCurrentPage().getMaxY() - 10);
            updateButtonVisibility();
        }
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public boolean isWithinRange(int pageNumber) {
        return pageNumber >= 0 && pageNumber < pages.size();
    }

    public ShowRecipePage getCurrentPage() {
        return pages.get(currentPageNumber);
    }

    public void order(int index) {
        RecipeData data = getCurrentPage().datas.get(index);
        boolean added = false;
        for (int i = 0;i < orders.size();i++) {
            var pair = orders.get(i);
            if (pair.left().ID.equals(data.ID) && pair.left().result.getCount() * (pair.right() + 1) <= pair.left().result.getMaxStackSize()) {
                orders.set(i,Pair.of(pair.left(),pair.right() + 1));
                added = true;
                initOrderTags();
                break;
            }
        }

        if (!added && orders.size() < 7) {
            orders.add(Pair.of(data,1));
            initOrderTags();
        }
    }

    public void cancelOrder(int index) {
        if (index < orders.size()) {
            orders.remove(index);
            initOrderTags();
        }
    }

    static {
        mc = Minecraft.getInstance();
        font = mc.font;
    }
}
