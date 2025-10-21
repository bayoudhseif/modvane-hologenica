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

        // Start BFS from source
        queue.add(sourcePos);
        visited.add(sourcePos);

        while (!queue.isEmpty()) {
            // Prevent searching too far
            if (visited.size() > 64) break;

            BlockPos current = queue.poll();

            // Check all 6 directions
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);

                if (visited.contains(neighborPos)) continue;
                visited.add(neighborPos);

                BlockState neighborState = level.getBlockState(neighborPos);

                // If it's a neurolink, add to search queue
                if (neighborState.getBlock() instanceof NeurolinkBlock) {
                    queue.add(neighborPos);
                    continue;
                }

                // If it's a neurocell, check if valid
                if (neighborState.getBlock() instanceof NeurocellBlock) {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof NeurocellBlockEntity neurocell) {
                        if (neurocell.acceptsConnectionFrom(dir.getOpposite()) && filter.test(neurocell)) {
                            return neurocell;
                        }
                    }
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
