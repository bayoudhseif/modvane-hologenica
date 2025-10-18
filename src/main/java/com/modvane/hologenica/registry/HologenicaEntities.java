package com.modvane.hologenica.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.modvane.hologenica.HologenicaMod;

public class HologenicaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, HologenicaMod.MODID);

    public static void init(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
