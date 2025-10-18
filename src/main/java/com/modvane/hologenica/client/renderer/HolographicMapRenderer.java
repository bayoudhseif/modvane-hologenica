package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.HolographicMapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix3f;


// Renders a 3D holographic projection of terrain above the holographic map block
public class HolographicMapRenderer implements BlockEntityRenderer<HolographicMapBlockEntity> {

    // Full brightness lightmap value for hologram glow
    private static final int LIGHTMAP = 0xF000F0;

    // UV coordinate for block rendering
    private static final float UV = 0.5f;

    // Alpha value for hologram transparency (0-255, 220 is slightly transparent)
    private static final int HOLOGRAM_ALPHA = 220;

    // No longer needed - colors are stored directly in terrain array

    public HolographicMapRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HolographicMapBlockEntity map, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (!map.hasValidRegion() || map.getLevel() == null) return;

        // Get scan bounds
        int minX = map.getScanMinX();
        int maxX = map.getScanMaxX();
        int minZ = map.getScanMinZ();
        int maxZ = map.getScanMaxZ();
        
        int scanWidth = maxX - minX + 1;
        int scanDepth = maxZ - minZ + 1;

        // Use cached terrain if available, otherwise scan and cache it
        int[][][] terrain = map.getCachedTerrain();
        if (terrain == null || map.needsRescan()) {
            terrain = scanTerrain(map.getLevel(), minX, maxX, minZ, maxZ);
            if (terrain == null) return;
            map.setCachedTerrain(terrain);
        }

        // Render the hologram
        poseStack.pushPose();
        poseStack.translate(0.5, 2.0, 0.5);

        // Rotate the hologram slowly over time (if rotation is enabled)
        if (map.isRotationEnabled()) {
            long time = map.getLevel().getGameTime();
            float rotation = (time + partialTick) * 0.5f;
            poseStack.mulPose(new org.joml.Quaternionf().rotationY((float) Math.toRadians(rotation)));
        }

        // Scale hologram to fit the desired block size
        int blockSize = map.getBlockSize();
        float scale = (float) blockSize / Math.max(scanWidth, scanDepth);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-scanWidth / 2.0f, 0, -scanDepth / 2.0f);

        renderTerrain(poseStack, buffer, terrain, scanWidth, scanDepth, map.isTransparentMode());

        poseStack.popPose();
    }

    // Scans terrain in the specified region and returns a 3D array of map colors
    private int[][][] scanTerrain(Level level, int minX, int maxX, int minZ, int maxZ) {
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        // Find Y bounds with blocks
        int minY = level.getMaxBuildHeight();
        int maxY = level.getMinBuildHeight();
        boolean foundBlocks = false;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // First pass: find Y bounds
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

        // If no blocks found, create minimal array
        if (!foundBlocks) {
            minY = level.getMinBuildHeight();
            maxY = level.getMinBuildHeight();
        }

        int height = Math.max(1, maxY - minY + 1);
        int[][][] terrain = new int[width][height][depth];

        // Second pass: scan all blocks and store map colors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockState state = level.getBlockState(pos.set(minX + x, minY + y, minZ + z));
                    if (!state.isAir()) {
                        terrain[x][y][z] = state.getMapColor(null, null).col;
                    }
                }
            }
        }

        return terrain;
    }

    private void renderTerrain(PoseStack poseStack, MultiBufferSource buffer, int[][][] terrain, int width, int depth, boolean transparent) {
        // Validate terrain array
        if (terrain == null || terrain.length != width || terrain[0].length == 0 || terrain[0][0].length != depth) {
            return;
        }
        
        // Render terrain blocks with optional transparency
        RenderType renderType = transparent ? RenderType.translucent() : RenderType.solid();
        int alpha = transparent ? HOLOGRAM_ALPHA : 255;

        VertexConsumer terrainConsumer = buffer.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int height = terrain[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    int color = terrain[x][y][z];
                    if (color != 0) { // 0 means air/empty
                        renderBlockOptimized(terrainConsumer, matrix, normal, terrain, x, y, z, width, height, depth, color, alpha);
                    }
                }
            }
        }
    }

    // Face culling - super simple rule:
    // If face is NOT touching a solid block, render it
    // If face IS touching a solid block, don't render it
    private void renderBlockOptimized(VertexConsumer c, Matrix4f m, Matrix3f n, int[][][] terrain,
                                      int x, int y, int z, int width, int height, int depth, int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float x1 = x, y1 = y, z1 = z;
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        // For each face, check if there's a solid block on the other side
        // If no solid block (air or out of bounds), render the face

        // Top face: render if no solid block above
        if (y + 1 >= height || terrain[x][y + 1][z] == 0) {
            quad(c, m, n, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, 0, 1, 0, r, g, b, alpha);
        }

        // Bottom face: render if no solid block below
        if (y - 1 < 0 || terrain[x][y - 1][z] == 0) {
            quad(c, m, n, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0, -1, 0, r, g, b, alpha);
        }

        // North face: render if no solid block to north
        if (z - 1 < 0 || terrain[x][y][z - 1] == 0) {
            quad(c, m, n, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, 0, 0, -1, r, g, b, alpha);
        }

        // South face: render if no solid block to south
        if (z + 1 >= depth || terrain[x][y][z + 1] == 0) {
            quad(c, m, n, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, 0, 0, 1, r, g, b, alpha);
        }

        // West face: render if no solid block to west
        if (x - 1 < 0 || terrain[x - 1][y][z] == 0) {
            quad(c, m, n, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, -1, 0, 0, r, g, b, alpha);
        }

        // East face: render if no solid block to east
        if (x + 1 >= width || terrain[x + 1][y][z] == 0) {
            quad(c, m, n, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, 1, 0, 0, r, g, b, alpha);
        }
    }

    // Renders a quad (4 vertices forming a rectangle)
    private void quad(VertexConsumer c, Matrix4f m, Matrix3f n,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      float x3, float y3, float z3, float x4, float y4, float z4,
                      float nx, float ny, float nz, int r, int g, int b, int a) {
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
    }

    // Colors are now stored directly in terrain array - no need for separate color lookup
}

