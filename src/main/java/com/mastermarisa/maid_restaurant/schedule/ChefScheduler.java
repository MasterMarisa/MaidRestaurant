package com.mastermarisa.maid_restaurant.schedule;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.data.request.CookingRequest;
import com.mastermarisa.maid_restaurant.data.task_data.ChefInformation;
import com.mastermarisa.maid_restaurant.data.task_data.WorkBlockCache;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import com.mastermarisa.maid_restaurant.item.ChefLicenseItem;
import com.mastermarisa.maid_restaurant.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.tree.NodeState;
import com.mastermarisa.maid_restaurant.uitls.BlockUsageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

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
        ChefInformation chefInfo = maid.getData(ModTaskDataKeys.CHEF_INFO);
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
        ChefInformation chefInfo = maid.getData(ModTaskDataKeys.CHEF_INFO);
        if (chefInfo == null) {
            return null;
        }
        return chefInfo.getStorageZone();
    }

    /**
     * 获取该女仆定义的备餐区
     * @param maid 女仆实体
     * @return 备餐区
     */
    @Nullable
    public static AbstractZone getPrepZone(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        ChefInformation chefInfo = maid.getData(ModTaskDataKeys.CHEF_INFO);
        if (chefInfo == null) {
            return null;
        }
        return chefInfo.getPrepZone();
    }

    @Nullable
    public static String getRestaurantId(EntityMaid maid) {
        ItemStack license = getChefLicense(maid);
        if (license.isEmpty()) {
            return null;
        }
        String restaurantId = ChefLicenseItem.getRestaurantId(license);
        if (restaurantId.isEmpty()) {
            return null;
        }
        return restaurantId;
    }

    /**
     * 获取或认领当前女仆对应的委托
     * @param level 所在世界
     * @param maid  女仆实体
     * @return 当前已认领或新认领的委托
     */
    @Nullable
    public static CookingRequest getOrClaimRequest(ServerLevel level, EntityMaid maid) {
        String restaurantId = getRestaurantId(maid);
        if (restaurantId == null) {
            return null;
        }
        CookingRequestBus bus = CookingRequestBus.getInstance(level);
        CookingRequest request = bus.getClaimed(restaurantId, maid);
        if (request == null) {
            request = bus.claim(restaurantId, maid);
            if (request != null) {
                request.root.verifyAndUpdateState(level, maid);
            }
        }
        return request;
    }

    /**
     * 释放当前女仆占用的委托
     * @param level 所在世界
     * @param maid  女仆实体
     */
    public static void releaseRequest(ServerLevel level, EntityMaid maid) {
        String restaurantId = getRestaurantId(maid);
        if (restaurantId != null) {
            CookingRequestBus.getInstance(level).release(restaurantId, maid);
        }
    }

    public static void submitRequest(ServerLevel level, EntityMaid maid) {
        String restaurantId = getRestaurantId(maid);
        if (restaurantId == null) {
            return;
        }
        CookingRequestBus bus = CookingRequestBus.getInstance(level);
        CookingRequest request = bus.submit(restaurantId, maid);
        if (request != null && request.boundRequest != null) {
            ServeRequestBus.getInstance(level).enqueue(restaurantId, request.boundRequest);
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
        return request.root.findNode(state);
    }

    public static List<ItemStack> getExistedInputs(ServerLevel level, EntityMaid maid, @Nullable ExecutionNode node) {
        if (node == null || node.isLeaf()) {
            return List.of();
        }

        WorkBlockCache cache = maid.getData(ModTaskDataKeys.WORK_BLOCK_CACHE);
        if (cache == null || BlockUsageUtil.isUsed(cache.getPos())) {
            return List.of();
        }

        ICookCapability capability = node.getCapability();
        if (capability == null || !capability.getUID().equals(cache.getCapabilityUID())) {
            return List.of();
        }

        AbstractZone zone = ChefScheduler.getWorkZone(maid);
        if (zone == null || !zone.contains(cache.getPos())) {
            return List.of();
        }

        if (capability.isValidWorkBlock(level, cache.getPos()) && zone.contains(cache.getPos())) {
            return capability.getExistedInputs(level, cache.getPos(), node.getRecipeNode());
        }
        return List.of();
    }
}
