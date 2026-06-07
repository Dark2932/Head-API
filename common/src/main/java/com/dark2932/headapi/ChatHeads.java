package com.dark2932.headapi;

import com.dark2932.headapi.mixininterface.HeadRenderable;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChatHeads {
    public static final String MOD_ID = HeadAPI.MOD_ID;

    // Blended head textures that have been extracted and registered
    public static final Set<ResourceLocation> blendedHeadTextures = new HashSet<>();
    private static final Map<ResourceLocation, ResourceLocation> skinToHeadLocation = new HashMap<>();
    private static final Map<UUID, ResourceLocation> uuidToSkinLocation = new HashMap<>();

    // State for passing head data through the mixin pipeline
    @NotNull public static HeadData lineData = HeadData.EMPTY;
    @NotNull public static HeadData refreshingLineData = HeadData.EMPTY;
    public static boolean refreshing;

    // Rendering context for FontStringRenderOutputMixin (set per drawString call)
    public static GuiGraphics guiGraphics;
    @NotNull public static HeadData renderHeadData = HeadData.EMPTY;
    public static float renderHeadOpacity;

    public static void init() {
    }

    // --- State management for GuiMessageLineMixin ---

    @NotNull
    public static HeadData getLineData() {
        return refreshing ? refreshingLineData : lineData;
    }

    public static void setLineData(@NotNull HeadData data) {
        if (refreshing) {
            refreshingLineData = data;
        } else {
            lineData = data;
        }
    }

    // --- Head data accessors for GuiMessage/GuiMessageLine ---

    @NotNull
    public static HeadData getHeadData(@NotNull net.minecraft.client.GuiMessage.Line guiMessage) {
        return ((HeadRenderable) (Object) guiMessage).headapi$getHeadData();
    }

    // --- Sender detection (called from ChatListenerMixin) ---

    public static void handleAddedMessage(@Nullable UUID senderUUID) {
        setLineData(HeadData.of(senderUUID));
    }

    public static void handleLocalPlayerMessage(@Nullable UUID senderUUID) {
        setLineData(HeadData.atEndOfLine(senderUUID));
    }

    // --- Head texture management ---

    public static void onSkinLoaded(ResourceLocation skinLocation, NativeImage skinImage) {
        String path = skinLocation.getPath();
        if (!path.startsWith("skins/")) return;

        NativeImage blendedHead = extractBlendedHead(skinImage);
        ResourceLocation headLocation = getBlendedHeadLocation(skinLocation);

        Minecraft.getInstance().getTextureManager()
                .register(headLocation, new DynamicTexture(blendedHead));

        blendedHeadTextures.add(skinLocation);
    }

    public static NativeImage extractBlendedHead(NativeImage skin) {
        boolean isLegacy = skin.getWidth() / 2 == skin.getHeight();
        int xScale = skin.getWidth() / 64;
        int yScale = skin.getHeight() / (isLegacy ? 32 : 64);

        NativeImage head = new NativeImage(8 * xScale, 8 * yScale, false);
        for (int y = 0; y < head.getHeight(); y++) {
            for (int x = 0; x < head.getWidth(); x++) {
                int headColor = skin.getPixelRGBA(8 * xScale + x, 8 * yScale + y);
                head.setPixelRGBA(x, y, headColor);
                if (!isLegacy) {
                    int hatColor = skin.getPixelRGBA(40 * xScale + x, 8 * yScale + y);
                    head.blendPixel(x, y, hatColor);
                }
            }
        }
        return head;
    }

    public static ResourceLocation getBlendedHeadLocation(ResourceLocation skinLocation) {
        return skinToHeadLocation.computeIfAbsent(skinLocation,
                loc -> ResourceLocation.fromNamespaceAndPath(MOD_ID, loc.getPath()));
    }

    @Nullable
    public static ResourceLocation getHeadSkinLocation(UUID uuid) {
        ResourceLocation cached = uuidToSkinLocation.get(uuid);
        if (cached != null && blendedHeadTextures.contains(cached)) {
            return cached;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return null;

        PlayerInfo info = connection.getPlayerInfo(uuid);
        if (info == null) return null;

        PlayerSkin skin = info.getSkin();
        ResourceLocation skinLocation = skin.texture();
        if (blendedHeadTextures.contains(skinLocation)) {
            uuidToSkinLocation.put(uuid, skinLocation);
            return skinLocation;
        }
        return null;
    }

    // --- Rendering ---

    public static int headWidth() {
        return 10; // 8px head + 2px padding
    }

    public static int getChatOffset(@NotNull HeadData headData) {
        return headData.isEmpty() ? 0 : headWidth();
    }

    public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, UUID uuid, float opacity) {
        ResourceLocation skinLocation = getHeadSkinLocation(uuid);
        if (skinLocation == null) return;

        ResourceLocation headLocation = getBlendedHeadLocation(skinLocation);
        if (!blendedHeadTextures.contains(skinLocation)) return;

        if (opacity != 1.0f) {
            RenderSystem.enableBlend();
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        guiGraphics.blit(headLocation, x, y, 8, 8, 0, 0, 8, 8, 8, 8);

        if (opacity != 1.0f) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}
