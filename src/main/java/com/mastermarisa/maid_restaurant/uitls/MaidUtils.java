package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerPlayer;

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
}
