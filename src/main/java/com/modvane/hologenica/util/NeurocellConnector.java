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
                // Now check all 4 directions from the neurolink for a neurocell
                for (Direction secondDir : Direction.Plane.HORIZONTAL) {
                    BlockPos neurocellPos = neurolinkPos.relative(secondDir);
                    BlockState neurocellState = level.getBlockState(neurocellPos);

                    if (neurocellState.getBlock() instanceof NeurocellBlock) {
                        BlockEntity be = level.getBlockEntity(neurocellPos);
                        if (be instanceof NeurocellBlockEntity neurocell) {
                            // Apply the filter to check if this neurocell is valid
                            if (filter.test(neurocell)) {
                                return neurocell;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    // Find a neurocell connected through exactly one neurolink from the back that matches the filter
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
                // Now check all 4 directions from the neurolink for a neurocell
                for (Direction secondDir : Direction.Plane.HORIZONTAL) {
                    BlockPos neurocellPos = neurolinkPos.relative(secondDir);
                    BlockState neurocellState = level.getBlockState(neurocellPos);

                    if (neurocellState.getBlock() instanceof NeurocellBlock) {
                        BlockEntity be = level.getBlockEntity(neurocellPos);
                        if (be instanceof NeurocellBlockEntity neurocell) {
                            // Check if connection is from the back and filter matches
                            if (neurocell.isBackConnection(secondDir.getOpposite()) && filter.test(neurocell)) {
                                return neurocell;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
