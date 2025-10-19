package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.entity.CloningPodBlockEntity;
import com.modvane.hologenica.block.entity.HologramProjectorBlockEntity;
import com.modvane.hologenica.block.entity.ReconstructionPodBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all block entities
public class HologenicaBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HologenicaMod.MODID);

    // Hologram projector block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HologramProjectorBlockEntity>> HOLOGRAM_PROJECTOR =
        BLOCK_ENTITIES.register("hologram_projector", () ->
            BlockEntityType.Builder.of(HologramProjectorBlockEntity::new,
                HologenicaBlocks.HOLOGRAM_PROJECTOR.get()).build(null));

    // Cloning pod block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CloningPodBlockEntity>> CLONING_POD =
        BLOCK_ENTITIES.register("cloning_pod", () ->
            BlockEntityType.Builder.of(CloningPodBlockEntity::new,
                HologenicaBlocks.CLONING_POD.get()).build(null));

    // Reconstruction pod block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReconstructionPodBlockEntity>> RECONSTRUCTION_POD =
        BLOCK_ENTITIES.register("reconstruction_pod", () ->
            BlockEntityType.Builder.of(ReconstructionPodBlockEntity::new,
                HologenicaBlocks.RECONSTRUCTION_POD.get()).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
