package com.modvane.code47.registry;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.modvane.code47.Code47Mod;

public class Code47Keybinds {
    // Example keybind
    // public static final KeyMapping EXAMPLE_KEY = new KeyMapping(
    //     "key." + Code47Mod.MODID + ".example",    // Translation key
    //     InputConstants.KEY_G,                      // Default key
    //     "key.categories." + Code47Mod.MODID       // Category translation key
    // );

    public static void init(IEventBus modEventBus) {
        // Register key bindings
        // modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
        //     event.register(EXAMPLE_KEY);
        // });
    }
}
