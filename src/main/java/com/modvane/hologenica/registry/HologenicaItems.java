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

    // Hologram pod item
    public static final DeferredHolder<Item, BlockItem> HOLOGRAM_POD =
        ITEMS.register("hologram_pod", () ->
            new BlockItem(HologenicaBlocks.HOLOGRAM_POD.get(), new Item.Properties()));

    // Cloning chamber item
    public static final DeferredHolder<Item, BlockItem> CLONING_CHAMBER =
        ITEMS.register("cloning_chamber", () ->
            new BlockItem(HologenicaBlocks.CLONING_CHAMBER.get(), new Item.Properties()));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
