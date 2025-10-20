package com.modvane.hologenica.client;

import com.modvane.hologenica.client.renderer.NeurocellRenderer;
import com.modvane.hologenica.client.renderer.HologramRenderer;
import com.modvane.hologenica.client.renderer.ReformerRenderer;
import com.modvane.hologenica.client.renderer.PlayerCloneRenderer;
import com.modvane.hologenica.client.screen.NeurocellScreen;
import com.modvane.hologenica.client.screen.HologramScreen;
import com.modvane.hologenica.client.screen.PlayerCloneScreen;
import com.modvane.hologenica.client.screen.TelepadScreen;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.registry.HologenicaBlocks;
import com.modvane.hologenica.registry.HologenicaEntities;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Client-side initialization for renderers and other client-only code
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HologenicaClient {

    // Register block entity renderers
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.HOLOGRAM.get(),
            HologramRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.NEUROCELL.get(),
            NeurocellRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.REFORMER.get(),
            ReformerRenderer::new
        );
        
        // Register entity renderers
        event.registerEntityRenderer(HologenicaEntities.PLAYER_CLONE.get(), PlayerCloneRenderer::new);
    }

    // Register GUI screens
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HologenicaMenus.HOLOGRAM.get(), HologramScreen::new);
        event.register(HologenicaMenus.NEUROCELL.get(), NeurocellScreen::new);
        event.register(HologenicaMenus.PLAYER_CLONE.get(), PlayerCloneScreen::new);
        event.register(HologenicaMenus.TELEPAD.get(), TelepadScreen::new);
    }

    // Set render layer for transparency support
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.HOLOGRAM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.NEUROCELL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.CENTRIFUGE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.REFORMER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.IMPRINTER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.TELEPAD.get(), RenderType.translucent());
        });
    }
}
