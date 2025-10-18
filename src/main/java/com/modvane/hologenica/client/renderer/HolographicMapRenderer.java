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

public class HolographicMapRenderer implements BlockEntityRenderer<HolographicMapBlockEntity> {

    private static final int LIGHTMAP = 0xF000F0;
    private static final float UV = 0.5f;

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

        BlockState[][][] terrain = scanTerrain(map.getLevel(), minX, maxX, minZ, maxZ);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.3, 0.5);

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        float scale = 3.5f / Math.max(width, depth);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-width / 2.0f, 0, -depth / 2.0f);

        renderTerrain(poseStack, buffer, terrain);

        poseStack.popPose();
    }

    private BlockState[][][] scanTerrain(Level level, int minX, int maxX, int minZ, int maxZ) {
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        int minY = level.getMaxBuildHeight();
        int maxY = level.getMinBuildHeight();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Find height bounds
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

        // Scan blocks
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

    private void renderTerrain(PoseStack poseStack, MultiBufferSource buffer, BlockState[][][] terrain) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int width = terrain.length;
        int height = terrain[0].length;
        int depth = terrain[0][0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockState state = terrain[x][y][z];
                    if (state != null) {
                        renderBlock(consumer, matrix, normal, x, y, z, getColor(state));
                    }
                }
            }
        }
    }

    private void renderBlock(VertexConsumer c, Matrix4f m, Matrix3f n, float x, float y, float z, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float x1 = x, y1 = y, z1 = z;
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        // Top
        quad(c, m, n, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0, 1, 0, r, g, b);
        // Bottom
        quad(c, m, n, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0, -1, 0, r, g, b);
        // North
        quad(c, m, n, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, 0, 0, -1, r, g, b);
        // South
        quad(c, m, n, x1, y2, z2, x2, y2, z2, x2, y1, z2, x1, y1, z2, 0, 0, 1, r, g, b);
        // West
        quad(c, m, n, x1, y2, z1, x1, y2, z2, x1, y1, z2, x1, y1, z1, -1, 0, 0, r, g, b);
        // East
        quad(c, m, n, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, 1, 0, 0, r, g, b);
    }

    private void quad(VertexConsumer c, Matrix4f m, Matrix3f n,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      float x3, float y3, float z3, float x4, float y4, float z4,
                      float nx, float ny, float nz, int r, int g, int b) {
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, 255).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, 255).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x3, y3, z3).setColor(r, g, b, 255).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
        c.addVertex(m, x4, y4, z4).setColor(r, g, b, 255).setUv(UV, UV).setLight(LIGHTMAP).setNormal(nx, ny, nz);
    }

    private int getColor(BlockState state) {
        return colorCache.computeIfAbsent(state, s -> s.getMapColor(null, null).col);
    }
}
