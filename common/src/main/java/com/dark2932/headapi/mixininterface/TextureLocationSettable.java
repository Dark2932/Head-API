package com.dark2932.headapi.mixininterface;

import net.minecraft.resources.ResourceLocation;

public interface TextureLocationSettable {
    void headapi$setTextureLocation(ResourceLocation location);

    ResourceLocation headapi$getTextureLocation();
}
