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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

// Simple pathfinding through neurolink connections
public class NeurocellConnector {

    // Find a connected neurocell through neurolink network
    @Nullable
    public static NeurocellBlockEntity findConnectedNeurocell(
        Level level,
        BlockPos sourcePos,
        Predicate<NeurocellBlockEntity> filter
    ) {
        if (level == null) return null;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        int neurolinkCount = 0;

        // Start BFS from source
        queue.add(sourcePos);
        visited.add(sourcePos);

        while (!queue.isEmpty()) {
            // Prevent searching too far (max 16 neurolinks in the network)
            if (neurolinkCount > 16) break;

            BlockPos current = queue.poll();

            // Check all 6 directions
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);

                if (visited.contains(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);

                // If it's a neurocell, check if valid (don't add to visited unless it's a neurolink)
                if (neighborState.getBlock() instanceof NeurocellBlock) {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof NeurocellBlockEntity neurocell) {
                        if (neurocell.acceptsConnectionFrom(dir.getOpposite()) && filter.test(neurocell)) {
                            return neurocell;
                        }
                    }
                    // Don't add neurocell to visited - allow checking from other directions
                    continue;
                }

                // Add to visited after checking it's not a neurocell
                visited.add(neighborPos);

                // If it's a neurolink, add to search queue and count it
                if (neighborState.getBlock() instanceof NeurolinkBlock) {
                    queue.add(neighborPos);
                    neurolinkCount++;
                    continue;
                }
            }
        }

        return null;
    }

    // Alias for imprinter (uses same logic)
    @Nullable
    public static NeurocellBlockEntity findConnectedNeurocellFromBack(
        Level level,
        BlockPos sourcePos,
        Predicate<NeurocellBlockEntity> filter
    ) {
        return findConnectedNeurocell(level, sourcePos, filter);
    }
}
