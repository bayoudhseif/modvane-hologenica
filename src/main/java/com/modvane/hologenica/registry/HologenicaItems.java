package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HologenicaItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HologenicaMod.MODID);

    // Holographic map item
    public static final DeferredHolder<Item, BlockItem> HOLOGRAPHIC_MAP =
        ITEMS.register("holographic_map", () ->
            new BlockItem(HologenicaBlocks.HOLOGRAPHIC_MAP.get(), new Item.Properties()));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
