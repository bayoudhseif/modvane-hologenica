package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.item.BioscannerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HologenicaItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HologenicaMod.MODID);

    // Hologram projector item
    public static final DeferredHolder<Item, BlockItem> HOLOGRAM_PROJECTOR =
        ITEMS.register("hologram_projector", () ->
            new BlockItem(HologenicaBlocks.HOLOGRAM_PROJECTOR.get(), new Item.Properties()));

    // Cloning pod item
    public static final DeferredHolder<Item, BlockItem> CLONING_POD =
        ITEMS.register("cloning_pod", () ->
            new BlockItem(HologenicaBlocks.CLONING_POD.get(), new Item.Properties()));

    // Bioscanner item
    public static final DeferredHolder<Item, BioscannerItem> BIOSCANNER =
        ITEMS.register("bioscanner", () ->
            new BioscannerItem(new Item.Properties().stacksTo(1)));

    // DNA centrifuge item
    public static final DeferredHolder<Item, BlockItem> DNA_CENTRIFUGE =
        ITEMS.register("dna_centrifuge", () ->
            new BlockItem(HologenicaBlocks.DNA_CENTRIFUGE.get(), new Item.Properties()));

    // Reconstruction pod item
    public static final DeferredHolder<Item, BlockItem> RECONSTRUCTION_POD =
        ITEMS.register("reconstruction_pod", () ->
            new BlockItem(HologenicaBlocks.RECONSTRUCTION_POD.get(), new Item.Properties()));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
