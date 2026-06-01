package com.dark2932.headapi.head;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeadCodepointMap {
    private static final int BASE_CODEPOINT = 0xE000;
    private static final int MAX_CODEPOINT = 0xF8FF;
    private static final Map<UUID, Integer> UUID_TO_CODEPOINT = new HashMap<>();
    private static final Map<Integer, UUID> CODEPOINT_TO_UUID = new HashMap<>();
    private static int nextIndex = 0;

    public static int getCodepoint(UUID uuid) {
        return UUID_TO_CODEPOINT.computeIfAbsent(uuid, u -> {
            if (nextIndex > MAX_CODEPOINT - BASE_CODEPOINT) {
                return BASE_CODEPOINT;
            }
            int cp = BASE_CODEPOINT + nextIndex++;
            CODEPOINT_TO_UUID.put(cp, u);
            return cp;
        });
    }

    public static UUID getUUID(int codepoint) {
        return CODEPOINT_TO_UUID.get(codepoint);
    }

    public static boolean isHeadCodepoint(int codepoint) {
        return codepoint >= BASE_CODEPOINT && codepoint <= MAX_CODEPOINT
                && CODEPOINT_TO_UUID.containsKey(codepoint);
    }

    public static IntSet getSupportedCodepoints() {
        return new IntOpenHashSet(CODEPOINT_TO_UUID.keySet());
    }
}
