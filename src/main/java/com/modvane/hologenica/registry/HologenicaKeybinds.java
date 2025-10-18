package com.modvane.hologenica.registry;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.modvane.hologenica.HologenicaMod;

public class HologenicaKeybinds {
    // Example keybind
    // public static final KeyMapping EXAMPLE_KEY = new KeyMapping(
    //     "key." + HologenicaMod.MODID + ".example",    // Translation key
    //     InputConstants.KEY_G,                      // Default key
    //     "key.categories." + HologenicaMod.MODID       // Category translation key
    // );

    public static void init(IEventBus modEventBus) {
        // Register key bindings
        // modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
        //     event.register(EXAMPLE_KEY);
        // });
    }
}
