package com.modvane.hologenica.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// Network packet registration for Hologenica
public class HologenicaNetwork {

    // Register all packets
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(HologenicaNetwork::onRegisterPayloadHandler);
    }

    private static void onRegisterPayloadHandler(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Register client-to-server packets
        registrar.playToServer(
            SetTelepadNamePacket.TYPE,
            SetTelepadNamePacket.STREAM_CODEC,
            SetTelepadNamePacket::handle
        );
    }
}
