package com.mastermarisa.maid_restaurant.core.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.tree.NodeState;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.uitls.ItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ChefScheduler {
    /**
     * 获取女仆装备的厨师执照
     * @param maid 女仆
     * @return 执照物品,若无则返还 ItemStack.EMPTY
     */
    public static ItemStack getChefLicense(EntityMaid maid) {
        BaubleItemHandler handler = maid.getMaidBauble();
        int index = handler.getBaubleSlot(BaubleManager.getBauble(ModItems.CHEF_LICENSE));
        if (index == -1) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(index);
    }

    /**
     * 获取该女仆定义的工作区
     * @param maid 女仆实体
     * @return 工作区
     */
    @Nullable
    public static AbstractZone getWorkZone(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        ChefInfo chefInfo = maid.getData(ModTaskDataKeys.CHEF_INFO);
        if (chefInfo == null) {
            return null;
        }
        return chefInfo.getWorkZone();
    }

    /**
     * 获取该女仆定义的储存区
     * @param maid 女仆实体
     * @return 储存区
     */
    @Nullable
    public static AbstractZone getStorageZone(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        ChefInfo chefInfo = maid.getData(ModTaskDataKeys.CHEF_INFO);
        if (chefInfo == null) {
            return null;
        }
        return chefInfo.getStorageZone();
    }

    /**
     * 获取或认领当前女仆对应的委托
     * @param level 所在世界
     * @param maid  女仆实体
     * @return 当前已认领或新认领的委托
     */
    @Nullable
    public static CookingRequest getOrClaimRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String restaurantId = ChefLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return null;
        }
        CookingRequestBus bus = CookingRequestBus.get(level);
        CookingRequest request = bus.getClaimed(restaurantId, maid);
        if (request != null) {
            return request;
        }
        return bus.claim(restaurantId, level, maid);
    }

    /**
     * 释放当前女仆占用的委托，并尝试从当前位置往后循环认领另一个未被占用的委托
     * @param level 所在世界
     * @param maid  女仆实体
     * @return 新认领的委托
     */
    @Nullable
    public static CookingRequest reclaimRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String restaurantId = ChefLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return null;
        }
        return CookingRequestBus.get(level).reclaim(restaurantId, level, maid);
    }

    /**
     * 释放当前女仆占用的委托
     * @param level 所在世界
     * @param maid  女仆实体
     */
    public static void releaseRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return;
        }
        String restaurantId = ChefLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return;
        }
        CookingRequestBus.get(level).release(restaurantId, level, maid);
    }

    public static void trySubmitRequest(ServerLevel level, EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return;
        }
        String restaurantId = ChefLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return;
        }
        CookingRequestBus bus = CookingRequestBus.get(level);
        CookingRequest request = bus.getClaimed(restaurantId, maid);
        if (request == null) {
            return;
        }
        ExecutionNode root = request.getRoot();
        RecipeNode recipeNode = root.getRecipeNode();
        int count = ItemUtils.count(maid.getAvailableInv(false), recipeNode.getOutput());
        if (count >= recipeNode.getOutputCount()) {
            bus.submit(restaurantId, maid);
            MaidRestaurant.LOGGER.debug("[MaidRestaurant-DEBUG] Context Submitted.");
        }
    }

    /**
     * 查找当前女仆委托树中第一个状态匹配的节点
     * @param level 所在世界
     * @param maid  女仆实体
     * @param state 目标节点状态
     * @return 第一个匹配的节点
     */
    @Nullable
    public static ExecutionNode findNode(ServerLevel level, EntityMaid maid, NodeState state) {
        CookingRequest request = getOrClaimRequest(level, maid);
        if (request == null) {
            return null;
        }
        return request.getRoot().findNode(state);
    }
}
