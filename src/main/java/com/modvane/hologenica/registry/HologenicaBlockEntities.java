package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.entity.NeurocellBlockEntity;
import com.modvane.hologenica.block.entity.HologramBlockEntity;
import com.modvane.hologenica.block.entity.ReformerBlockEntity;
import com.modvane.hologenica.block.entity.TelepadBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all block entities
public class HologenicaBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HologenicaMod.MODID);

    // Hologram block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HologramBlockEntity>> HOLOGRAM =
        BLOCK_ENTITIES.register("hologram", () ->
            BlockEntityType.Builder.of(HologramBlockEntity::new,
                HologenicaBlocks.HOLOGRAM.get()).build(null));

    // Neurocell block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NeurocellBlockEntity>> NEUROCELL =
        BLOCK_ENTITIES.register("neurocell", () ->
            BlockEntityType.Builder.of(NeurocellBlockEntity::new,
                HologenicaBlocks.NEUROCELL.get()).build(null));

    // Reformer block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReformerBlockEntity>> REFORMER =
        BLOCK_ENTITIES.register("reformer", () ->
            BlockEntityType.Builder.of(ReformerBlockEntity::new,
                HologenicaBlocks.REFORMER.get()).build(null));

    // Telepad block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TelepadBlockEntity>> TELEPAD =
        BLOCK_ENTITIES.register("telepad", () ->
            BlockEntityType.Builder.of(TelepadBlockEntity::new,
                HologenicaBlocks.TELEPAD.get()).build(null));

    public static void init(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
