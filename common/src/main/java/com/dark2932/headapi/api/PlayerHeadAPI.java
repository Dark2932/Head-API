package com.dark2932.headapi.api;

import com.dark2932.headapi.head.HeadCodepointMap;
import com.dark2932.headapi.head.HeadTextureManager;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class PlayerHeadAPI {
    public static ResourceLocation getHeadTexture(UUID uuid) {
        return HeadTextureManager.getOrRegisterHeadTexture(uuid);
    }

    public static ResourceLocation getHeadTexture(String playerName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return null;
        }
        var info = mc.getConnection().getOnlinePlayers().stream()
                .filter(p -> p.getProfile().getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
        if (info == null) {
            return null;
        }
        return getHeadTexture(info.getProfile().getId());
    }

    public static boolean isHeadAvailable(UUID uuid) {
        return HeadTextureManager.hasHead(uuid);
    }

    public static PlayerSkin getPlayerSkin(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        SkinManager sm = mc.getSkinManager();
        if (sm == null) {
            return null;
        }
        var info = mc.getConnection() != null
                ? mc.getConnection().getPlayerInfo(uuid)
                : null;
        if (info != null) {
            return info.getSkin();
        }
        return null;
    }

    public static Component createHeadComponent(UUID uuid) {
        int codepoint = HeadCodepointMap.getCodepoint(uuid);
        return Component.literal(new String(Character.toChars(codepoint)))
                .withStyle(Style.EMPTY);
    }

    public static MutableComponent appendHead(MutableComponent component, UUID uuid) {
        return component.append(createHeadComponent(uuid));
    }

    public static Component wrapWithHead(UUID uuid, Component text) {
        return Component.empty()
                .append(createHeadComponent(uuid))
                .append(Component.literal(" "))
                .append(text);
    }
}
