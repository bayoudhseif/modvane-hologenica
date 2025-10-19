package com.modvane.hologenica.client;

import com.modvane.hologenica.client.renderer.CloningChamberRenderer;
import com.modvane.hologenica.client.renderer.HologramPodRenderer;
import com.modvane.hologenica.client.renderer.ReconstructionPodRenderer;
import com.modvane.hologenica.client.screen.DNACentrifugeScreen;
import com.modvane.hologenica.client.screen.HologramPodScreen;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.registry.HologenicaBlocks;
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
            HologenicaBlockEntities.HOLOGRAM_POD.get(),
            HologramPodRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.CLONING_CHAMBER.get(),
            CloningChamberRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.RECONSTRUCTION_POD.get(),
            ReconstructionPodRenderer::new
        );
    }

    // Register GUI screens
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HologenicaMenus.HOLOGRAM_POD.get(), HologramPodScreen::new);
        event.register(HologenicaMenus.DNA_CENTRIFUGE.get(), DNACentrifugeScreen::new);
    }

    // Set render layer for transparency support
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.HOLOGRAM_POD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.CLONING_CHAMBER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.DNA_CENTRIFUGE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.RECONSTRUCTION_POD.get(), RenderType.translucent());
        });
    }
}
