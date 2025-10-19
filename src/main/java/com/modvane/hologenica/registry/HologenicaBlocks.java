package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.block.BridgeBlock;
import com.modvane.hologenica.block.CentrifugeBlock;
import com.modvane.hologenica.block.CloningPodBlock;
import com.modvane.hologenica.block.HologramProjectorBlock;
import com.modvane.hologenica.block.ReconstructionPodBlock;
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

    // Hologram projector block
    public static final DeferredHolder<Block, HologramProjectorBlock> HOLOGRAM_PROJECTOR =
        BLOCKS.register("hologram_projector", () -> new HologramProjectorBlock(
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

    // Cloning pod block
    public static final DeferredHolder<Block, CloningPodBlock> CLONING_POD =
        BLOCKS.register("cloning_pod", () -> new CloningPodBlock(
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

    // Reconstruction pod block
    public static final DeferredHolder<Block, ReconstructionPodBlock> RECONSTRUCTION_POD =
        BLOCKS.register("reconstruction_pod", () -> new ReconstructionPodBlock(
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

    // Bridge block - decorative walkway
    public static final DeferredHolder<Block, BridgeBlock> BRIDGE =
        BLOCKS.register("bridge", () -> new BridgeBlock(
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
