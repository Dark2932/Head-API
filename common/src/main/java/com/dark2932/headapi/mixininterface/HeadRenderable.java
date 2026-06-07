package com.dark2932.headapi.mixininterface;

import com.dark2932.headapi.HeadData;
import org.jetbrains.annotations.NotNull;

public interface HeadRenderable {
    @NotNull HeadData headapi$getHeadData();
    void headapi$setHeadData(@NotNull HeadData headData);
}
