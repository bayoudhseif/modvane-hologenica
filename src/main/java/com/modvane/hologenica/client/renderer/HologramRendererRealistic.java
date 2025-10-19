package com.modvane.hologenica.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// New "realistic" rendering style - uses custom render types with pure vertex colors
// Gives a cleaner, more modern appearance
public class HologramRendererRealistic {

    // Full brightness for hologram
    private static final int FULL_BRIGHT = 0xF000F0;

    // Custom render type that uses only vertex colors (no textures) - SOLID
    private static final RenderType HOLOGRAM_SOLID = RenderType.create(
        "hologenica_hologram_solid",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        256,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
            .createCompositeState(false)
    );

    // Custom render type for transparent hologram with proper depth handling
    private static final RenderType HOLOGRAM_TRANSLUCENT = RenderType.create(
        "hologenica_hologram_translucent",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE) // Write both color and depth
            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
            .setCullState(RenderStateShard.NO_CULL) // Don't cull faces
            .createCompositeState(false)
    );

    // Render the realistic-style terrain
    public static void render(PoseStack poseStack, MultiBufferSource buffer, int[][][] terrain, 
                             int width, int depth, boolean transparent) {
        
        if (terrain == null || terrain.length != width || terrain[0].length == 0 || terrain[0][0].length != depth) {
            return;
        }

        int height = terrain[0].length;

        // Choose render type based on transparency setting
        RenderType renderType = transparent ? HOLOGRAM_TRANSLUCENT : HOLOGRAM_SOLID;
        VertexConsumer consumer = buffer.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Render each block
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    int color = terrain[x][y][z];
                    if (color != 0) {
                        renderBlock(consumer, matrix, normal, terrain, x, y, z, width, height, depth, color, transparent);
                    }
                }
            }
        }
    }

    // Scan terrain and get map colors
    public static int[][][] scanTerrain(Level level, int minX, int maxX, int minZ, int maxZ) {
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Find Y range
        int minY = level.getMaxBuildHeight();
        int maxY = level.getMinBuildHeight();
        boolean foundBlocks = false;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (!state.isAir()) {
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                        foundBlocks = true;
                        break;
                    }
                }
            }
        }

        if (!foundBlocks) {
            minY = level.getMinBuildHeight();
            maxY = level.getMinBuildHeight();
        }

        int height = Math.max(1, maxY - minY + 1);
        int[][][] terrain = new int[width][height][depth];

        // Scan and store colors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockState state = level.getBlockState(pos.set(minX + x, minY + y, minZ + z));
                    if (!state.isAir()) {
                        terrain[x][y][z] = state.getMapColor(level, pos).col;
                    }
                }
            }
        }

        return terrain;
    }

    // Render a single block with face culling
    private static void renderBlock(VertexConsumer c, Matrix4f m, Matrix3f n, int[][][] terrain,
                                    int x, int y, int z, int width, int height, int depth, int color, boolean transparent) {
        
        // Extract RGB from map color
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = transparent ? 180 : 255; // Semi-transparent or solid

        float x1 = x, y1 = y, z1 = z;
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        // Top
        if (y + 1 >= height || terrain[x][y + 1][z] == 0) {
            addQuad(c, m, n, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, 0, 1, 0, r, g, b, a);
        }

        // Bottom
        if (y - 1 < 0 || terrain[x][y - 1][z] == 0) {
            addQuad(c, m, n, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0, -1, 0, r, g, b, a);
        }

        // North
        if (z - 1 < 0 || terrain[x][y][z - 1] == 0) {
            addQuad(c, m, n, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, 0, 0, -1, r, g, b, a);
        }

        // South
        if (z + 1 >= depth || terrain[x][y][z + 1] == 0) {
            addQuad(c, m, n, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, 0, 0, 1, r, g, b, a);
        }

        // West
        if (x - 1 < 0 || terrain[x - 1][y][z] == 0) {
            addQuad(c, m, n, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, -1, 0, 0, r, g, b, a);
        }

        // East
        if (x + 1 >= width || terrain[x + 1][y][z] == 0) {
            addQuad(c, m, n, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, 1, 0, 0, r, g, b, a);
        }
    }

    // Add a quad with pure vertex color (no textures, no UVs needed)
    private static void addQuad(VertexConsumer c, Matrix4f m, Matrix3f n,
                               float x1, float y1, float z1, float x2, float y2, float z2,
                               float x3, float y3, float z3, float x4, float y4, float z4,
                               float nx, float ny, float nz, int r, int g, int b, int a) {
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setLight(FULL_BRIGHT);
    }
}

