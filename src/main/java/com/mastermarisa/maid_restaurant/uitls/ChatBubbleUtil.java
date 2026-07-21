package com.mastermarisa.maid_restaurant.uitls;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManager;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatBubbleUtil {
    private static final Map<UUID, Long> cacheMap = new ConcurrentHashMap<>();

    public static void setChatBubble(EntityMaid maid, IChatBubbleData bubble) {
        ChatBubbleManager manager = maid.getChatBubbleManager();
        if (cacheMap.containsKey(maid.getUUID())) {
            manager.removeChatBubble(cacheMap.get(maid.getUUID()));
        }
        cacheMap.put(maid.getUUID(), manager.addChatBubble(bubble));
    }

    public static void setTextChatBubble(EntityMaid maid, Component component) {
        setChatBubble(maid, TextChatBubbleData.type2(component));
    }

    public static void removeChatBubble(EntityMaid maid) {
        ChatBubbleManager manager = maid.getChatBubbleManager();
        if (cacheMap.containsKey(maid.getUUID())) {
            manager.removeChatBubble(cacheMap.get(maid.getUUID()));
        }
    }
}
