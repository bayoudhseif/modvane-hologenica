package com.modvane.hologenica.client;

import com.modvane.hologenica.client.renderer.HolographicMapRenderer;
import com.modvane.hologenica.client.screen.HolographicMapScreen;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Client-side initialization for renderers and other client-only code
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HologenicaClient {

    // Register holographic map renderer
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            HologenicaBlockEntities.HOLOGRAPHIC_MAP.get(),
            HolographicMapRenderer::new
        );
    }

    // Register GUI screens
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HologenicaMenus.HOLOGRAPHIC_MAP.get(), HolographicMapScreen::new);
    }
}
