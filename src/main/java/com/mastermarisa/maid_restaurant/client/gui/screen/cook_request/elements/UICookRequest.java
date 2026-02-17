package com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.elements;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.client.gui.UIConst;
import com.mastermarisa.maid_restaurant.client.gui.base.*;
import com.mastermarisa.maid_restaurant.client.gui.screen.cook_request.CookRequestScreen;
import com.mastermarisa.maid_restaurant.network.ModifyAttributePayload;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class UICookRequest extends UIElement {
    protected final UIImage bg;
    protected final UIItemStack toServe;
    protected final UILabel count;
    protected final UICancelRequestButton btn;
    protected final UICycleAttribute cycleAttribute;
    protected final UIStockingModeButton stockingAttribute;

    public UICookRequest(int index, CookRequestScreen screen) {
        super(new Rectangle(18,70));
        bg = new UIImage(UIConst.requestImage);

        CookRequest request = Objects.requireNonNull(screen.handler.getAt(index));
        ICookTask iCookTask = CookTasks.getTask(request.type);
        Level level = screen.maid.level();
        ItemStack result = iCookTask.getResult(level.getRecipeManager().byKey(request.id).get(),level);
        result = result.copyWithCount(result.getCount() * request.requested);

        toServe = new UIItemStack(result);
        count = new UILabel(String.valueOf(result.getCount()), UILabel.TextAlignment.LEFT, UIConst.lessBlack, false);
        cycleAttribute = new UICycleAttribute(index,screen);
        stockingAttribute = new UIStockingModeButton(index,screen);
        btn = new UICancelRequestButton(index,screen);

        children = List.of(bg,toServe,count,btn,cycleAttribute,stockingAttribute);
    }

    @Override
    protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
        resize();
        super.render(graphics, mouseX, mouseY);
    }

    public void resize() {
        bg.setCenter(getCenterX(),getCenterY());
        toServe.setCenterX(getCenterX());
        toServe.setMinY(getMinY() + 23);
        count.setCenterX(getCenterX());
        count.setMinY(toServe.getMaxY() + 2);
        btn.setCenterX(getCenterX());
        btn.setMinY(count.getMaxY() + 4);
        cycleAttribute.setCenterX(getCenterX());
        cycleAttribute.setMinY(getMaxY() + 3);
        stockingAttribute.setCenterX(getCenterX());
        stockingAttribute.setMinY(cycleAttribute.getMaxY() + 3);
    }

    public static class UICycleAttribute extends UIButton {
        private final int index;
        private final CookRequestScreen screen;
        private final CookRequest.Attributes attributes;
        private final UIImage poweredImg;
        private final UIImage unpoweredImg;

        public UICycleAttribute(int index, CookRequestScreen screen) {
            super(new Rectangle(14,16),(button) -> ((UICycleAttribute)button).trigger(),0);
            this.index = index;
            this.screen = screen;
            this.attributes = Objects.requireNonNull(screen.handler.getAt(index)).attributes;
            poweredImg = new UIImage(UIConst.cycle_powered);
            unpoweredImg = new UIImage(UIConst.cycle_unpowered);
            poweredImg.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.cycle_on"));
            unpoweredImg.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.cycle_off"));
        }

        @Override
        protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            if (attributes.cycle()) {
                poweredImg.setCenter(getCenterX(),getCenterY());
                UIElement.render(graphics,poweredImg,mouseX,mouseY);
            } else {
                unpoweredImg.setCenter(getCenterX(),getCenterY());
                UIElement.render(graphics,unpoweredImg,mouseX,mouseY);
            }
        }

        public void trigger() {
            if (!screen.checkAvailability()) return;
            attributes.setCycle(!attributes.cycle());
            ModifyAttributePayload payload = new ModifyAttributePayload(0,screen.maid.getUUID(),index,attributes.getAttributes());
            PacketDistributor.sendToServer(payload);
        }
    }

    @ParametersAreNonnullByDefault
    public static class UIStockingModeButton extends UIButton {
        private final int index;
        private final CookRequestScreen screen;
        private final CookRequest.Attributes attributes;
        private final UIItemStack disabled;
        private final UIItemStack insertable;
        private final UIItemStack spaceEnough;

        public UIStockingModeButton(int index, CookRequestScreen screen) {
            super(new Rectangle(16,16), (button) -> ((UIStockingModeButton)button).trigger(), 0);
            this.index = index;
            this.screen = screen;
            this.attributes = Objects.requireNonNull(screen.handler.getAt(index)).attributes;
            disabled = new UIItemStack(new ItemStack(ModItems.TABLE_OAK.get()));
            insertable = new UIItemStack(new ItemStack(Items.HOPPER));
            spaceEnough = new UIItemStack(new ItemStack(Items.CHEST));
            disabled.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_unlimited"));
            insertable.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_insertable"));
            spaceEnough.tooltip.add(Component.translatable("gui.maid_restaurant.cook_request.stocking_mode_space_enough"));
        }

        @Override
        protected void render(GuiGraphics graphics, int mouseX, int mouseY) {
            super.render(graphics, mouseX, mouseY);
            switch (attributes.getStockingMode().id) {
                case 0 -> {
                    disabled.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,disabled,mouseX,mouseY);
                }
                case 1 -> {
                    insertable.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,insertable,mouseX,mouseY);
                }
                case 2 -> {
                    spaceEnough.setCenter(getCenterX(),getCenterY());
                    UIElement.render(graphics,spaceEnough,mouseX,mouseY);
                }
            }
        }

        public void trigger() {
            if (!screen.checkAvailability()) return;
            attributes.getAttributes()[1]++;
            if (attributes.getAttributes()[1] > 2) attributes.getAttributes()[1] = 0;
            ModifyAttributePayload payload = new ModifyAttributePayload(0,screen.maid.getUUID(),index,attributes.getAttributes());
            PacketDistributor.sendToServer(payload);
        }
    }
}
