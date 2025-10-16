package com.modvane.code47.registry;

import com.modvane.code47.Code47Mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Code47Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Code47Mod.MODID);

    // Add item definitions here

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
