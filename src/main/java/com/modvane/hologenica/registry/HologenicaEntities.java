package com.modvane.hologenica.registry;

import com.modvane.hologenica.entity.SteveNPCEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.modvane.hologenica.HologenicaMod;

public class HologenicaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, HologenicaMod.MODID);

    // Steve NPC entity - spawned when cloning a player
    public static final DeferredHolder<EntityType<?>, EntityType<SteveNPCEntity>> STEVE_NPC =
        ENTITY_TYPES.register("steve_npc", () -> EntityType.Builder.of(SteveNPCEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.8F) // Player-like size
            .clientTrackingRange(8)
            .build("steve_npc"));

    public static void init(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        
        // Register entity attributes
        modEventBus.addListener(HologenicaEntities::registerAttributes);
    }
    
    // Register entity attributes
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(STEVE_NPC.get(), SteveNPCEntity.createAttributes().build());
    }
}
