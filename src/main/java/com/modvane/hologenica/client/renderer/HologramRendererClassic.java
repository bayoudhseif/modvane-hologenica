package com.modvane.hologenica.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

// NEW INNOVATIVE CLASSIC RENDERER
// Uses volumetric layering with depth-based color gradients and animated horizontal scan planes
// Creates a unique "building up" hologram effect with color shift based on height
public class HologramRendererClassic {

    private static final int FULL_BRIGHT = 0xF000F0;

    // Custom render type for volumetric quads with proper depth handling
    private static final RenderType VOLUMETRIC_QUADS = RenderType.create(
        "hologenica_volumetric",
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


    // Render with volumetric layering
    public static void render(PoseStack poseStack, MultiBufferSource buffer, int[][][] terrain, 
                             int width, int depth, boolean transparent) {
        
        if (terrain == null || terrain.length != width || terrain[0].length == 0 || terrain[0][0].length != depth) {
            return;
        }

        int height = terrain[0].length;
        
        VertexConsumer consumer = buffer.getBuffer(VOLUMETRIC_QUADS);
        Matrix4f matrix = poseStack.last().pose();

        // Render blocks with volumetric depth-based color shifting
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    int color = terrain[x][y][z];
                    if (color != 0) {
                        renderVolumetricBlock(consumer, matrix, terrain, x, y, z, width, height, depth, 
                                            color, transparent);
                    }
                }
            }
        }
    }

    // Render block with volumetric depth-based effects
    private static void renderVolumetricBlock(VertexConsumer c, Matrix4f m, int[][][] terrain,
                                             int x, int y, int z, int width, int height, int depth,
                                             int color, boolean transparent) {
        
        // Extract base color
        int baseR = (color >> 16) & 0xFF;
        int baseG = (color >> 8) & 0xFF;
        int baseB = color & 0xFF;
        
        // Height-based gradient: lower blocks = cooler (blue shift), higher = warmer (cyan shift)
        float heightRatio = (float) y / height;
        
        // Apply depth-based color transformation
        // Low = more blue, High = more cyan/white
        int r = (int)(baseR * (0.4f + heightRatio * 0.6f) + 50 * heightRatio);
        int g = (int)(baseG * (0.4f + heightRatio * 0.6f) + 120 * heightRatio);
        int b = (int)(baseB * (0.5f + heightRatio * 0.5f) + 180 - 80 * heightRatio);
        
        // Clamp
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));
        
        // Alpha (transparent mode uses what non-transparent was, non-transparent is fully opaque)
        int a = transparent ? 200 : 255;
        
        float x1 = x, y1 = y, z1 = z;
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;
        
        // Render each face with slight inset for layered look
        float inset = 0.02f;

        // Top (fixed vertex winding order for proper culling)
        if (y + 1 >= height || terrain[x][y + 1][z] == 0) {
            addQuad(c, m, 
                x1 + inset, y2, z2 - inset, 
                x2 - inset, y2, z2 - inset, 
                x2 - inset, y2, z1 + inset, 
                x1 + inset, y2, z1 + inset, 
                r, g, b, a);
        }

        // Bottom (fixed vertex winding order)
        if (y - 1 < 0 || terrain[x][y - 1][z] == 0) {
            addQuad(c, m, 
                x1 + inset, y1, z1 + inset, 
                x2 - inset, y1, z1 + inset, 
                x2 - inset, y1, z2 - inset, 
                x1 + inset, y1, z2 - inset, 
                r, g, b, a);
        }

        // All sides use same alpha as top for consistency
        
        // North
        if (z - 1 < 0 || terrain[x][y][z - 1] == 0) {
            addQuad(c, m, 
                x2 - inset, y1 + inset, z1, 
                x1 + inset, y1 + inset, z1, 
                x1 + inset, y2 - inset, z1, 
                x2 - inset, y2 - inset, z1, 
                r, g, b, a);
        }

        // South
        if (z + 1 >= depth || terrain[x][y][z + 1] == 0) {
            addQuad(c, m, 
                x1 + inset, y1 + inset, z2, 
                x2 - inset, y1 + inset, z2, 
                x2 - inset, y2 - inset, z2, 
                x1 + inset, y2 - inset, z2, 
                r, g, b, a);
        }

        // West
        if (x - 1 < 0 || terrain[x - 1][y][z] == 0) {
            addQuad(c, m, 
                x1, y1 + inset, z1 + inset, 
                x1, y1 + inset, z2 - inset, 
                x1, y2 - inset, z2 - inset, 
                x1, y2 - inset, z1 + inset, 
                r, g, b, a);
        }

        // East
        if (x + 1 >= width || terrain[x + 1][y][z] == 0) {
            addQuad(c, m, 
                x2, y1 + inset, z2 - inset, 
                x2, y1 + inset, z1 + inset, 
                x2, y2 - inset, z1 + inset, 
                x2, y2 - inset, z2 - inset, 
                r, g, b, a);
        }
    }

    // Add a quad
    private static void addQuad(VertexConsumer c, Matrix4f m,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float x3, float y3, float z3, float x4, float y4, float z4,
                                int r, int g, int b, int a) {
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setLight(FULL_BRIGHT);
        c.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setLight(FULL_BRIGHT);
    }

    // Scan terrain
    public static int[][][] scanTerrain(Level level, int minX, int maxX, int minZ, int maxZ) {
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

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
}
