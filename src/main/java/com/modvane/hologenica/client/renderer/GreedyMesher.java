package com.modvane.hologenica.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

// Ultra-optimized greedy meshing algorithm
// Combines adjacent same-color blocks into larger quads for maximum performance
public class GreedyMesher {

    private static final int FULL_BRIGHT = 0xF000F0;

    // A merged quad represents multiple blocks combined into one larger face
    public static class MergedQuad {
        public final float x1, y1, z1, x2, y2, z2;
        public final int r, g, b, a;
        public final Direction dir;

        public MergedQuad(float x1, float y1, float z1, float x2, float y2, float z2,
                         int r, int g, int b, int a, Direction dir) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.dir = dir;
        }

        // Render this merged quad
        public void render(VertexConsumer c, Matrix4f m, Matrix3f n) {
            switch (dir) {
                case UP -> addQuad(c, m,
                    x1, y2, z2,
                    x2, y2, z2,
                    x2, y2, z1,
                    x1, y2, z1,
                    r, g, b, a);
                case DOWN -> addQuad(c, m,
                    x1, y1, z1,
                    x2, y1, z1,
                    x2, y1, z2,
                    x1, y1, z2,
                    r, g, b, a);
                case NORTH -> addQuad(c, m,
                    x2, y1, z1,
                    x1, y1, z1,
                    x1, y2, z1,
                    x2, y2, z1,
                    r, g, b, a);
                case SOUTH -> addQuad(c, m,
                    x1, y1, z2,
                    x2, y1, z2,
                    x2, y2, z2,
                    x1, y2, z2,
                    r, g, b, a);
                case WEST -> addQuad(c, m,
                    x1, y1, z1,
                    x1, y1, z2,
                    x1, y2, z2,
                    x1, y2, z1,
                    r, g, b, a);
                case EAST -> addQuad(c, m,
                    x2, y1, z2,
                    x2, y1, z1,
                    x2, y2, z1,
                    x2, y2, z2,
                    r, g, b, a);
            }
        }

        private static void addQuad(VertexConsumer c, Matrix4f m,
                                   float x1, float y1, float z1, float x2, float y2, float z2,
                                   float x3, float y3, float z3, float x4, float y4, float z4,
                                   int r, int g, int b, int a) {
            c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setLight(FULL_BRIGHT);
            c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setLight(FULL_BRIGHT);
            c.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setLight(FULL_BRIGHT);
            c.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        }
    }

    public enum Direction {
        UP, DOWN, NORTH, SOUTH, WEST, EAST
    }

    // Generate optimized mesh for the entire terrain
    public static List<MergedQuad> generateMesh(int[][][] terrain, boolean transparent) {
        int width = terrain.length;
        int height = terrain[0].length;
        int depth = terrain[0][0].length;

        // Pre-allocate list with estimated capacity
        List<MergedQuad> quads = new ArrayList<>(width * height * depth / 4);

        int alpha = transparent ? 180 : 255;

        // Process each direction using optimized sweep algorithm
        meshDirectionOptimized(terrain, width, height, depth, Direction.UP, alpha, quads);
        meshDirectionOptimized(terrain, width, height, depth, Direction.DOWN, alpha, quads);
        meshDirectionOptimized(terrain, width, height, depth, Direction.NORTH, alpha, quads);
        meshDirectionOptimized(terrain, width, height, depth, Direction.SOUTH, alpha, quads);
        meshDirectionOptimized(terrain, width, height, depth, Direction.WEST, alpha, quads);
        meshDirectionOptimized(terrain, width, height, depth, Direction.EAST, alpha, quads);

        return quads;
    }

    // Optimized greedy meshing for a single direction
    private static void meshDirectionOptimized(int[][][] terrain, int width, int height, int depth,
                                               Direction dir, int alpha, List<MergedQuad> quads) {
        // Choose sweep axis based on direction
        int d1, d2, d3; // dimensions for sweep

        switch (dir) {
            case UP, DOWN -> {
                d1 = height;
                d2 = width;
                d3 = depth;
            }
            case NORTH, SOUTH -> {
                d1 = depth;
                d2 = width;
                d3 = height;
            }
            default -> { // WEST, EAST
                d1 = width;
                d2 = depth;
                d3 = height;
            }
        }

        // Sweep through slices perpendicular to face direction
        for (int i = 0; i < d1; i++) {
            // Create a 2D mask for this slice
            int[][] mask = new int[d2][d3];
            boolean[][] processed = new boolean[d2][d3];

            // Fill mask with colors that should be rendered
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    int x, y, z;

                    // Map sweep coordinates back to terrain coordinates
                    switch (dir) {
                        case UP -> {
                            x = j; y = i; z = k;
                            if (y + 1 >= height || terrain[x][y + 1][z] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                        case DOWN -> {
                            x = j; y = i; z = k;
                            if (y - 1 < 0 || terrain[x][y - 1][z] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                        case NORTH -> {
                            x = j; y = k; z = i;
                            if (z - 1 < 0 || terrain[x][y][z - 1] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                        case SOUTH -> {
                            x = j; y = k; z = i;
                            if (z + 1 >= depth || terrain[x][y][z + 1] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                        case WEST -> {
                            x = i; y = k; z = j;
                            if (x - 1 < 0 || terrain[x - 1][y][z] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                        case EAST -> {
                            x = i; y = k; z = j;
                            if (x + 1 >= width || terrain[x + 1][y][z] == 0) {
                                mask[j][k] = terrain[x][y][z];
                            }
                        }
                    }
                }
            }

            // Greedy mesh the 2D mask
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    if (processed[j][k] || mask[j][k] == 0) continue;

                    int color = mask[j][k];
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;

                    // Expand rectangle along first axis
                    int w = 1;
                    while (j + w < d2 && !processed[j + w][k] && mask[j + w][k] == color) {
                        w++;
                    }

                    // Expand rectangle along second axis
                    int h = 1;
                    boolean canExpand = true;
                    while (k + h < d3 && canExpand) {
                        for (int dj = 0; dj < w; dj++) {
                            if (processed[j + dj][k + h] || mask[j + dj][k + h] != color) {
                                canExpand = false;
                                break;
                            }
                        }
                        if (canExpand) h++;
                    }

                    // Mark all cells in rectangle as processed
                    for (int dj = 0; dj < w; dj++) {
                        for (int dk = 0; dk < h; dk++) {
                            processed[j + dj][k + dk] = true;
                        }
                    }

                    // Create quad with proper coordinates
                    float x1, y1, z1, x2, y2, z2;

                    switch (dir) {
                        case UP -> {
                            x1 = j; y1 = i; z1 = k;
                            x2 = j + w; y2 = i + 1; z2 = k + h;
                        }
                        case DOWN -> {
                            x1 = j; y1 = i; z1 = k;
                            x2 = j + w; y2 = i + 1; z2 = k + h;
                        }
                        case NORTH -> {
                            x1 = j; y1 = k; z1 = i;
                            x2 = j + w; y2 = k + h; z2 = i + 1;
                        }
                        case SOUTH -> {
                            x1 = j; y1 = k; z1 = i;
                            x2 = j + w; y2 = k + h; z2 = i + 1;
                        }
                        case WEST -> {
                            x1 = i; y1 = k; z1 = j;
                            x2 = i + 1; y2 = k + h; z2 = j + w;
                        }
                        default -> { // EAST
                            x1 = i; y1 = k; z1 = j;
                            x2 = i + 1; y2 = k + h; z2 = j + w;
                        }
                    }

                    quads.add(new MergedQuad(x1, y1, z1, x2, y2, z2, r, g, b, alpha, dir));
                }
            }
        }
    }
}
