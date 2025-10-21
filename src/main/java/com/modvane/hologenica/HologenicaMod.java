package com.modvane.hologenica;

import com.modvane.hologenica.network.HologenicaNetwork;
import com.modvane.hologenica.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

@Mod(HologenicaMod.MODID)
public class HologenicaMod {
    public static final String MODID = "hologenica";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a creative tab for our items
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> MOD_TAB =
        CREATIVE_TABS.register("mod_tab",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + MODID))
                .icon(() -> new ItemStack(HologenicaItems.NEUROCELL.get()))
                .displayItems((parameters, output) -> {
                    // Add all our items to the creative tab in logical order
                    output.accept(HologenicaItems.MANUAL.get());
                    output.accept(HologenicaItems.NEUROCELL.get());
                    output.accept(HologenicaItems.BIOSCANNER.get());
                    output.accept(HologenicaItems.REFORMER.get());
                    output.accept(HologenicaItems.IMPRINTER.get());
                    output.accept(HologenicaItems.NEUROLINK.get());
                    output.accept(HologenicaItems.TELEPAD.get());
                    output.accept(HologenicaItems.HOLOGRAM.get());
                })
                .build()
        );

    public HologenicaMod(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize registries
        HologenicaBlocks.init(modEventBus);
        HologenicaItems.init(modEventBus);
        HologenicaEntities.init(modEventBus);
        HologenicaBlockEntities.init(modEventBus);
        HologenicaMenus.init(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        // Initialize network
        HologenicaNetwork.register(modEventBus);

        LOGGER.info("Mod initialized");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
