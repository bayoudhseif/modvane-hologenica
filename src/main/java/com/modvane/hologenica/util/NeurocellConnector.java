package com.modvane.hologenica.util;

import com.modvane.hologenica.block.NeurocellBlock;
import com.modvane.hologenica.block.NeurolinkBlock;
import com.modvane.hologenica.block.entity.NeurocellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

// Utility for finding neurocell blocks connected through neurolink blocks
public class NeurocellConnector {

    // Find a neurocell connected through exactly one neurolink that matches the filter
    // Neurolink BACK connects to neurocell (any side except front), neurolink FRONT connects to source (reformer)
    @Nullable
    public static NeurocellBlockEntity findConnectedNeurocell(
        Level level,
        BlockPos sourcePos,
        Predicate<NeurocellBlockEntity> filter
    ) {
        if (level == null) return null;

        // Check all 4 horizontal directions for neurolinks
        for (Direction firstDir : Direction.Plane.HORIZONTAL) {
            BlockPos neurolinkPos = sourcePos.relative(firstDir);
            BlockState neurolinkState = level.getBlockState(neurolinkPos);

            // Must be a neurolink (no direct connection to neurocell)
            if (neurolinkState.getBlock() instanceof NeurolinkBlock) {
                // Get the neurolink's facing direction (this is the FRONT)
                Direction neurolinkFacing = neurolinkState.getValue(NeurolinkBlock.FACING);

                // The neurolink's FRONT must be pointing toward the source block
                // firstDir is the direction FROM source TO neurolink
                // So neurolinkFacing should be OPPOSITE of firstDir (facing back at source)
                if (neurolinkFacing != firstDir.getOpposite()) {
                    continue; // Neurolink's front is not pointing at source
                }

                // The neurolink's BACK is opposite to its facing
                Direction neurolinkBack = neurolinkFacing.getOpposite();

                // Look for neurocell only at the neurolink's BACK
                BlockPos neurocellPos = neurolinkPos.relative(neurolinkBack);
                BlockState neurocellState = level.getBlockState(neurocellPos);

                if (neurocellState.getBlock() instanceof NeurocellBlock) {
                    BlockEntity be = level.getBlockEntity(neurocellPos);
                    if (be instanceof NeurocellBlockEntity neurocell) {
                        // Verify the neurocell accepts this connection (not from its front)
                        // The direction from neurocell to neurolink
                        Direction neurocellToNeurolink = neurolinkBack.getOpposite();

                        if (neurocell.acceptsConnectionFrom(neurocellToNeurolink) && filter.test(neurocell)) {
                            return neurocell;
                        }
                    }
                }
            }
        }

        return null;
    }

    // Find a neurocell connected through exactly one neurolink from any valid side that matches the filter
    // Used by Imprinter - neurolink BACK connects to neurocell (any side except front), neurolink FRONT connects to imprinter
    @Nullable
    public static NeurocellBlockEntity findConnectedNeurocellFromBack(
        Level level,
        BlockPos sourcePos,
        Predicate<NeurocellBlockEntity> filter
    ) {
        if (level == null) return null;

        // Check all 4 horizontal directions for neurolinks
        for (Direction firstDir : Direction.Plane.HORIZONTAL) {
            BlockPos neurolinkPos = sourcePos.relative(firstDir);
            BlockState neurolinkState = level.getBlockState(neurolinkPos);

            // Must be a neurolink (no direct connection to neurocell)
            if (neurolinkState.getBlock() instanceof NeurolinkBlock) {
                // Get the neurolink's facing direction (this is the FRONT)
                Direction neurolinkFacing = neurolinkState.getValue(NeurolinkBlock.FACING);

                // The neurolink's FRONT must be pointing toward the source block (imprinter)
                // firstDir is the direction FROM source TO neurolink
                // So neurolinkFacing should be OPPOSITE of firstDir (facing back at source)
                if (neurolinkFacing != firstDir.getOpposite()) {
                    continue; // Neurolink's front is not pointing at source
                }

                // The neurolink's BACK is opposite to its facing
                Direction neurolinkBack = neurolinkFacing.getOpposite();

                // Look for neurocell only at the neurolink's BACK
                BlockPos neurocellPos = neurolinkPos.relative(neurolinkBack);
                BlockState neurocellState = level.getBlockState(neurocellPos);

                if (neurocellState.getBlock() instanceof NeurocellBlock) {
                    BlockEntity be = level.getBlockEntity(neurocellPos);
                    if (be instanceof NeurocellBlockEntity neurocell) {
                        // Verify the neurocell accepts this connection (not from its front)
                        // The direction from neurocell to neurolink
                        Direction neurocellToNeurolink = neurolinkBack.getOpposite();

                        if (neurocell.acceptsConnectionFrom(neurocellToNeurolink) && filter.test(neurocell)) {
                            return neurocell;
                        }
                    }
                }
            }
        }

        return null;
    }
}
