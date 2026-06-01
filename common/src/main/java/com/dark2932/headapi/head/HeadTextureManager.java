package com.dark2932.headapi.head;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class HeadTextureManager {
    public static final String NAMESPACE = "headapi";

    private static final Map<UUID, String> UUID_TO_MOJANG_HASH = new Object2ObjectOpenHashMap<>();
    private static final Map<String, NativeImage> SKIN_HASH_TO_HEAD = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, ResourceLocation> UUID_TO_HEAD_RL = new Object2ObjectOpenHashMap<>();

    public static void associateUUID(UUID uuid, String mojangHash) {
        UUID_TO_MOJANG_HASH.put(uuid, mojangHash);
    }

    public static void onSkinLoaded(ResourceLocation skinLocation, NativeImage skinImage) {
        String path = skinLocation.getPath();
        if (path.startsWith("skins/")) {
            String sha1Hash = path.substring("skins/".length());
            SKIN_HASH_TO_HEAD.put(sha1Hash, extractHead(skinImage));
        }
    }

    public static NativeImage extractHead(NativeImage skin) {
        int w = skin.getWidth();
        int h = skin.getHeight();
        boolean legacy = w / 2 == h;
        int xs = w / 64;
        int ys = h / (legacy ? 32 : 64);

        NativeImage head = new NativeImage(8 * xs, 8 * ys, false);
        for (int y = 0; y < head.getHeight(); y++) {
            for (int x = 0; x < head.getWidth(); x++) {
                head.setPixelRGBA(x, y, skin.getPixelRGBA(8 * xs + x, 8 * ys + y));
                if (!legacy) {
                    head.blendPixel(x, y, skin.getPixelRGBA(40 * xs + x, 8 * ys + y));
                }
            }
        }
        return head;
    }

    public static NativeImage getHeadImage(UUID uuid) {
        String sha1 = resolveSha1(uuid);
        if (sha1 == null) {
            return null;
        }
        return SKIN_HASH_TO_HEAD.get(sha1);
    }

    public static ResourceLocation getOrRegisterHeadTexture(UUID uuid) {
        ResourceLocation cached = UUID_TO_HEAD_RL.get(uuid);
        if (cached != null) {
            return cached;
        }

        NativeImage image = getHeadImage(uuid);
        if (image == null) {
            return null;
        }

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "heads/" + uuid);
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        tm.register(rl, new DynamicTexture(image));
        UUID_TO_HEAD_RL.put(uuid, rl);
        return rl;
    }

    public static boolean hasHead(UUID uuid) {
        String sha1 = resolveSha1(uuid);
        return sha1 != null && SKIN_HASH_TO_HEAD.containsKey(sha1);
    }

    private static String resolveSha1(UUID uuid) {
        String mojang = UUID_TO_MOJANG_HASH.get(uuid);
        if (mojang != null) {
            return Hashing.sha1().hashUnencodedChars(mojang).toString();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return null;
        }

        var info = mc.getConnection().getPlayerInfo(uuid);
        if (info == null) {
            return null;
        }

        PlayerSkin skin = info.getSkin();
        String path = skin.texture().getPath();
        if (path.startsWith("skins/")) {
            return path.substring("skins/".length());
        }
        return null;
    }
}
