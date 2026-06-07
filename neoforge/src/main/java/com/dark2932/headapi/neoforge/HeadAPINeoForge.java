package com.dark2932.headapi.neoforge;

import com.dark2932.headapi.ChatHeads;
import com.dark2932.headapi.HeadAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(HeadAPI.MOD_ID)
public final class HeadAPINeoForge {
    public HeadAPINeoForge() {
        HeadAPI.init();
        ChatHeads.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerChat(ClientChatReceivedEvent event) {
        if (event.isSystem()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!event.getSender().equals(player.getUUID())) return;

        ChatHeads.handleLocalPlayerMessage(player.getUUID());
    }
}
