package com.modvane.code47.registry;

import com.modvane.code47.Code47Mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Code47Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Code47Mod.MODID);

    // Add block definitions here

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
