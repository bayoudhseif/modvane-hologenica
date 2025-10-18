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

import java.util.HashMap;
import java.util.Map;

// Renders a 3D holographic projection of terrain above the holographic map block
public class HolographicMapRenderer implements BlockEntityRenderer<HolographicMapBlockEntity> {

    // Full brightness lightmap value for hologram glow
    private static final int LIGHTMAP = 0xF000F0;

    // UV coordinate for block rendering
    private static final float UV = 0.5f;

    // Alpha value for hologram transparency (0-255, 220 is slightly transparent)
    private static final int HOLOGRAM_ALPHA = 220;

    // Cache block state colors to avoid recalculating every frame
    private final Map<BlockState, Integer> colorCache = new HashMap<>();

    public HolographicMapRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HolographicMapBlockEntity map, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (!map.hasRegion() || map.getLevel() == null) return;

        BlockPos pos1 = map.getPos1();
        BlockPos pos2 = map.getPos2();

        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Use cached terrain if available, otherwise scan and cache it
        BlockState[][][] terrain = map.getCachedTerrain();
        if (terrain == null || map.needsRescan()) {
            terrain = scanTerrain(map.getLevel(), minX, maxX, minZ, maxZ);
            map.setCachedTerrain(terrain);
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.3, 0.5);

        // Rotate the hologram slowly over time
        long time = map.getLevel().getGameTime();
        float rotation = (time + partialTick) * 0.5f;
        poseStack.mulPose(new org.joml.Quaternionf().rotationY((float) Math.toRadians(rotation)));

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        float scale = 3.5f / Math.max(width, depth);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-width / 2.0f, 0, -depth / 2.0f);

        renderTerrain(poseStack, buffer, terrain, width, depth);

        poseStack.popPose();
    }

    // Scans terrain in the specified region and returns a 3D array of block states
    // This only runs once and gets cached - not every frame
    private BlockState[][][] scanTerrain(Level level, int minX, int maxX, int minZ, int maxZ) {
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        int minY = level.getMaxBuildHeight();
        int maxY = level.getMinBuildHeight();

        // Reusable position object to avoid creating thousands of BlockPos objects
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // First pass: find the minimum and maximum Y coordinates with blocks
        // This lets us create a smaller array and skip scanning empty vertical space
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
                    if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                        break;
                    }
                }
            }
        }

        int height = maxY - minY + 1;
        BlockState[][][] terrain = new BlockState[width][height][depth];

        // Second pass: scan all blocks in the region and store non-air blocks
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockState state = level.getBlockState(pos.set(minX + x, minY + y, minZ + z));
                    if (!state.isAir()) {
                        terrain[x][y][z] = state;
                    }
                }
            }
        }

        return terrain;
    }

    private void renderTerrain(PoseStack poseStack, MultiBufferSource buffer, BlockState[][][] terrain, int width, int depth) {
        // Render terrain blocks with slight transparency
        // Only render faces that are exposed (not hidden by other blocks)
        VertexConsumer terrainConsumer = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int height = terrain[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockState state = terrain[x][y][z];
                    if (state != null) {
                        renderBlockOptimized(terrainConsumer, matrix, normal, terrain, x, y, z, width, height, depth, getColor(state), HOLOGRAM_ALPHA);
                    }
                }
            }
        }
    }

    // Renders only the visible faces of a block (face culling optimization)
    // This drastically reduces rendering load by skipping faces hidden by adjacent blocks
    private void renderBlockOptimized(VertexConsumer c, Matrix4f m, Matrix3f n, BlockState[][][] terrain,
                                      int x, int y, int z, int width, int height, int depth, int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float x1 = x, y1 = y, z1 = z;
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        // Only render a face if there's no block adjacent to it
        // Top face (check block above)
        if (y + 1 >= height || terrain[x][y + 1][z] == null) {
            quad(c, m, n, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0, 1, 0, r, g, b, alpha);
        }
        // Bottom face (check block below)
        if (y - 1 < 0 || terrain[x][y - 1][z] == null) {
            quad(c, m, n, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0, -1, 0, r, g, b, alpha);
        }
        // North face (check block to north)
        if (z - 1 < 0 || terrain[x][y][z - 1] == null) {
            quad(c, m, n, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, 0, 0, -1, r, g, b, alpha);
        }
        // South face (check block to south)
        if (z + 1 >= depth || terrain[x][y][z + 1] == null) {
            quad(c, m, n, x1, y2, z2, x2, y2, z2, x2, y1, z2, x1, y1, z2, 0, 0, 1, r, g, b, alpha);
        }
        // West face (check block to west)
        if (x - 1 < 0 || terrain[x - 1][y][z] == null) {
            quad(c, m, n, x1, y2, z1, x1, y2, z2, x1, y1, z2, x1, y1, z1, -1, 0, 0, r, g, b, alpha);
        }
        // East face (check block to east)
        if (x + 1 >= width || terrain[x + 1][y][z] == null) {
            quad(c, m, n, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, 1, 0, 0, r, g, b, alpha);
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

    // Gets the color for a block state using Minecraft's map color system
    private int getColor(BlockState state) {
        return colorCache.computeIfAbsent(state, s -> s.getMapColor(null, null).col);
    }
}

