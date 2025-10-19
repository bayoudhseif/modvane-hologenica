package com.modvane.hologenica.client;

import com.modvane.hologenica.client.renderer.CloningPodRenderer;
import com.modvane.hologenica.client.renderer.HologramProjectorRenderer;
import com.modvane.hologenica.client.renderer.ReconstructionPodRenderer;
import com.modvane.hologenica.client.renderer.SteveNPCRenderer;
import com.modvane.hologenica.client.screen.CloningPodScreen;
import com.modvane.hologenica.client.screen.HologramProjectorScreen;
import com.modvane.hologenica.client.screen.SteveNPCScreen;
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
            HologenicaBlockEntities.HOLOGRAM_PROJECTOR.get(),
            HologramProjectorRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.CLONING_POD.get(),
            CloningPodRenderer::new
        );
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.RECONSTRUCTION_POD.get(),
            ReconstructionPodRenderer::new
        );
        
        // Register entity renderers
        event.registerEntityRenderer(HologenicaEntities.STEVE_NPC.get(), SteveNPCRenderer::new);
    }

    // Register GUI screens
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HologenicaMenus.HOLOGRAM_PROJECTOR.get(), HologramProjectorScreen::new);
        event.register(HologenicaMenus.CLONING_POD.get(), CloningPodScreen::new);
        event.register(HologenicaMenus.STEVE_NPC.get(), SteveNPCScreen::new);
    }

    // Set render layer for transparency support
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.HOLOGRAM_PROJECTOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.CLONING_POD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.CENTRIFUGE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HologenicaBlocks.RECONSTRUCTION_POD.get(), RenderType.translucent());
        });
    }
}
