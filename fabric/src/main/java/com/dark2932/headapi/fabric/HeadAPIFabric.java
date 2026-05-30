package com.dark2932.headapi.fabric;

import net.fabricmc.api.ModInitializer;

import com.dark2932.headapi.HeadAPI;

public final class HeadAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        HeadAPI.init();
    }
}
