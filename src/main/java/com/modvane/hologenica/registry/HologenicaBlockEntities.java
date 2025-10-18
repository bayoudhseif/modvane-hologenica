package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.entity.HolographicMapBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all block entities
public class HologenicaBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HologenicaMod.MODID);

    // Holographic map block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HolographicMapBlockEntity>> HOLOGRAPHIC_MAP =
        BLOCK_ENTITIES.register("holographic_map", () ->
            BlockEntityType.Builder.of(HolographicMapBlockEntity::new,
                HologenicaBlocks.HOLOGRAPHIC_MAP.get()).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
