package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.entity.CloningChamberBlockEntity;
import com.modvane.hologenica.block.entity.DNACentrifugeBlockEntity;
import com.modvane.hologenica.block.entity.HologramPodBlockEntity;
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

    // Hologram pod block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HologramPodBlockEntity>> HOLOGRAM_POD =
        BLOCK_ENTITIES.register("hologram_pod", () ->
            BlockEntityType.Builder.of(HologramPodBlockEntity::new,
                HologenicaBlocks.HOLOGRAM_POD.get()).build(null));

    // Cloning chamber block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CloningChamberBlockEntity>> CLONING_CHAMBER =
        BLOCK_ENTITIES.register("cloning_chamber", () ->
            BlockEntityType.Builder.of(CloningChamberBlockEntity::new,
                HologenicaBlocks.CLONING_CHAMBER.get()).build(null));

    // DNA centrifuge block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DNACentrifugeBlockEntity>> DNA_CENTRIFUGE =
        BLOCK_ENTITIES.register("dna_centrifuge", () ->
            BlockEntityType.Builder.of(DNACentrifugeBlockEntity::new,
                HologenicaBlocks.DNA_CENTRIFUGE.get()).build(null));

    // Reconstruction pod block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReconstructionPodBlockEntity>> RECONSTRUCTION_POD =
        BLOCK_ENTITIES.register("reconstruction_pod", () ->
            BlockEntityType.Builder.of(ReconstructionPodBlockEntity::new,
                HologenicaBlocks.RECONSTRUCTION_POD.get()).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
