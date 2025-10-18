package com.modvane.hologenica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// Cloning Chamber block for entity duplication
public class CloningChamberBlock extends Block {

    public CloningChamberBlock(Properties properties) {
        super(properties);
    }

    // Disable ambient occlusion for brighter rendering
    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f; // Full brightness, no shading
    }
}

