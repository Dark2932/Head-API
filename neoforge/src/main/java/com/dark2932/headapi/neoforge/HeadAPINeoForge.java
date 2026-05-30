package com.dark2932.headapi.neoforge;

import net.neoforged.fml.common.Mod;

import com.dark2932.headapi.HeadAPI;

@Mod(HeadAPI.MOD_ID)
public final class HeadAPINeoForge {
    public HeadAPINeoForge() {
        // Run our common setup.
        HeadAPI.init();
    }
}
