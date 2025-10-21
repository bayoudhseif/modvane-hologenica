package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.item.BioscannerItem;
import com.modvane.hologenica.item.ManualItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HologenicaItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HologenicaMod.MODID);

    // Hologram item
    public static final DeferredHolder<Item, BlockItem> HOLOGRAM =
        ITEMS.register("hologram", () ->
            new BlockItem(HologenicaBlocks.HOLOGRAM.get(), new Item.Properties()));

    // Neurocell item
    public static final DeferredHolder<Item, BlockItem> NEUROCELL =
        ITEMS.register("neurocell", () ->
            new BlockItem(HologenicaBlocks.NEUROCELL.get(), new Item.Properties()));

    // Bioscanner item
    public static final DeferredHolder<Item, BioscannerItem> BIOSCANNER =
        ITEMS.register("bioscanner", () ->
            new BioscannerItem(new Item.Properties().stacksTo(1)));

    // Hologenica Manual item
    public static final DeferredHolder<Item, ManualItem> MANUAL =
        ITEMS.register("manual", () ->
            new ManualItem(new Item.Properties().stacksTo(1)));

    // Centrifuge item
    public static final DeferredHolder<Item, BlockItem> CENTRIFUGE =
        ITEMS.register("centrifuge", () ->
            new BlockItem(HologenicaBlocks.CENTRIFUGE.get(), new Item.Properties()));

    // Reformer item
    public static final DeferredHolder<Item, BlockItem> REFORMER =
        ITEMS.register("reformer", () ->
            new BlockItem(HologenicaBlocks.REFORMER.get(), new Item.Properties()));

    // Imprinter item
    public static final DeferredHolder<Item, BlockItem> IMPRINTER =
        ITEMS.register("imprinter", () ->
            new BlockItem(HologenicaBlocks.IMPRINTER.get(), new Item.Properties()));

    // Telepad item
    public static final DeferredHolder<Item, BlockItem> TELEPAD =
        ITEMS.register("telepad", () ->
            new BlockItem(HologenicaBlocks.TELEPAD.get(), new Item.Properties()));

    // Neurolink item
    public static final DeferredHolder<Item, BlockItem> NEUROLINK =
        ITEMS.register("neurolink", () ->
            new BlockItem(HologenicaBlocks.NEUROLINK.get(), new Item.Properties()));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
