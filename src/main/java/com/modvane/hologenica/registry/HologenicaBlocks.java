package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.HolographicMapBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HologenicaBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, HologenicaMod.MODID);

    // Holographic map block
    public static final DeferredHolder<Block, HolographicMapBlock> HOLOGRAPHIC_MAP =
        BLOCKS.register("holographic_map", () -> new HolographicMapBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 10)
        ));

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
