package com.dark2932.headapi;

import com.dark2932.headapi.api.PlayerHeadAPI;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public final class ChatHeadHandler {
    public static Component appendHeadToMessage(UUID senderUUID, Component message) {
        if (senderUUID == null || !PlayerHeadAPI.isHeadAvailable(senderUUID)) {
            return message;
        }
        return PlayerHeadAPI.wrapWithHead(senderUUID, message);
    }
}
