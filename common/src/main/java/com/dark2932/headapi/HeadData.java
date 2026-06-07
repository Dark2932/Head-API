package com.dark2932.headapi;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record HeadData(@Nullable UUID uuid, int codePointIndex, boolean endOfLine) {
    public static final HeadData EMPTY = new HeadData(null, -1, false);

    public static HeadData of(@Nullable UUID uuid) {
        if (uuid == null) return EMPTY;
        return new HeadData(uuid, 0, false);
    }

    public static HeadData atEndOfLine(@Nullable UUID uuid) {
        if (uuid == null) return EMPTY;
        return new HeadData(uuid, -1, true);
    }

    public boolean isEmpty() {
        return uuid == null;
    }
}
