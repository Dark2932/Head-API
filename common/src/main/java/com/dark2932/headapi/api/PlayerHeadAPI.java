package com.dark2932.headapi.api;

import com.dark2932.headapi.ChatHeads;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PlayerHeadAPI {
    @Nullable
    public static ResourceLocation getHeadSkinLocation(UUID uuid) {
        return ChatHeads.getHeadSkinLocation(uuid);
    }

    @Nullable
    public static ResourceLocation getHeadSkinLocation(String playerName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return null;

        var info = mc.getConnection().getOnlinePlayers().stream()
                .filter(p -> p.getProfile().getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
        if (info == null) return null;

        return getHeadSkinLocation(info.getProfile().getId());
    }

    public static boolean isHeadAvailable(UUID uuid) {
        return ChatHeads.getHeadSkinLocation(uuid) != null;
    }

    @Nullable
    public static PlayerSkin getPlayerSkin(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return null;

        var info = mc.getConnection().getPlayerInfo(uuid);
        return info != null ? info.getSkin() : null;
    }
}
