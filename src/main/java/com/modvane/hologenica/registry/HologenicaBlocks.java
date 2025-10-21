package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.NeurolinkBlock;
import com.modvane.hologenica.block.CentrifugeBlock;
import com.modvane.hologenica.block.NeurocellBlock;
import com.modvane.hologenica.block.HologramBlock;
import com.modvane.hologenica.block.ImprinterBlock;
import com.modvane.hologenica.block.ReformerBlock;
import com.modvane.hologenica.block.TelepadBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HologenicaBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, HologenicaMod.MODID);

    // Hologram block
    public static final DeferredHolder<Block, HologramBlock> HOLOGRAM =
        BLOCKS.register("hologram", () -> new HologramBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15) // Max light level
                .noOcclusion() // Allow transparency
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Neurocell block
    public static final DeferredHolder<Block, NeurocellBlock> NEUROCELL =
        BLOCKS.register("neurocell", () -> new NeurocellBlock(
            BlockBehaviour.Properties.of()
                .strength(5.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 10)
                .noOcclusion() // Allow transparency for glass parts
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Centrifuge block
    public static final DeferredHolder<Block, CentrifugeBlock> CENTRIFUGE =
        BLOCKS.register("centrifuge", () -> new CentrifugeBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 8)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Reformer block
    public static final DeferredHolder<Block, ReformerBlock> REFORMER =
        BLOCKS.register("reformer", () -> new ReformerBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 12)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Imprinter block
    public static final DeferredHolder<Block, ImprinterBlock> IMPRINTER =
        BLOCKS.register("imprinter", () -> new ImprinterBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 8)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Telepad block
    public static final DeferredHolder<Block, TelepadBlock> TELEPAD =
        BLOCKS.register("telepad", () -> new TelepadBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f)
                .sound(SoundType.METAL)
                .lightLevel(state -> 10)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    // Neurolink block - connects like redstone
    public static final DeferredHolder<Block, NeurolinkBlock> NEUROLINK =
        BLOCKS.register("neurolink", () -> new NeurolinkBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        ));

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
