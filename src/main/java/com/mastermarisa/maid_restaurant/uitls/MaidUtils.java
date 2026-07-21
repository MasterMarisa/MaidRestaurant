package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.ICookCapability;
import com.mastermarisa.maid_restaurant.core.schedule.ChefScheduler;
import com.mastermarisa.maid_restaurant.core.schedule.WorkBlockCache;
import com.mastermarisa.maid_restaurant.core.tree.ExecutionNode;
import com.mastermarisa.maid_restaurant.core.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.init.ModTaskDataKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaidUtils {
    public static void sendMessageToOwner(EntityMaid maid, Component component) {
        if (maid.getOwner() instanceof ServerPlayer player) {
            ChatType.Bound bound = ChatType.bind(ChatType.CHAT, maid);
            OutgoingChatMessage message = new OutgoingChatMessage.Disguised(component);
            player.sendChatMessage(message, true, bound);
        }
    }

    public static double distSqrHorizontal(EntityMaid maid, BlockPos pos) {
        double dx = maid.getX() - (pos.getX() + 0.5);
        double dz = maid.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dz * dz;
    }

    public static void exchangeToHand(EntityMaid maid, InteractionHand hand, int index) {
        IItemHandler maidInv = maid.getAvailableInv(false);
        ItemStack remainder = maidInv.extractItem(index, 64, false);
        ItemStack itemInHand = maid.getItemInHand(hand).copyAndClear();
        if (!remainder.isEmpty()) {
            maid.setItemInHand(hand, remainder);
        }
        if (!itemInHand.isEmpty()) {
            maidInv.insertItem(index, itemInHand, false);
        }
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
