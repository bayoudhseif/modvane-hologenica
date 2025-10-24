package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.HologramBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

// Renders a 3D holographic projection of terrain above the hologram projector block
// Uses optimized greedy meshing for maximum performance
public class HologramRenderer implements BlockEntityRenderer<HologramBlockEntity> {

    public HologramRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HologramBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (!blockEntity.hasValidRegion() || blockEntity.getLevel() == null) return;

        Level level = blockEntity.getLevel();
        int minX = blockEntity.getScanMinX();
        int maxX = blockEntity.getScanMaxX();
        int minZ = blockEntity.getScanMinZ();
        int maxZ = blockEntity.getScanMaxZ();

        // Scan terrain if needed
        int[][][] terrain = blockEntity.getCachedTerrain();
        if (terrain == null || blockEntity.needsRescan()) {
            terrain = HologramRendererRealistic.scanTerrain(level, minX, maxX, minZ, maxZ);
            if (terrain == null) return;
            blockEntity.setCachedTerrain(terrain);
        }

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        // Generate or retrieve cached greedy mesh
        List<GreedyMesher.MergedQuad> mesh = blockEntity.getCachedMesh();
        if (mesh == null) {
            boolean transparent = blockEntity.isTransparentMode();
            mesh = GreedyMesher.generateMesh(terrain, transparent);
            blockEntity.setCachedMesh(mesh);
        }

        // Setup rendering transform
        poseStack.pushPose();
        poseStack.translate(0.5, 2.0, 0.5);

        // Rotation if enabled
        if (blockEntity.isRotationEnabled()) {
            long time = level.getGameTime();
            float rotation = (time + partialTick) * 0.5f;
            poseStack.mulPose(new org.joml.Quaternionf().rotationY((float) Math.toRadians(rotation)));
        }

        // Scale to fit
        int blockSize = blockEntity.getBlockSize();
        float scale = (float) blockSize / Math.max(width, depth);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-width / 2.0f, 0, -depth / 2.0f);

        // Render the greedy mesh
        boolean transparent = blockEntity.isTransparentMode();
        VertexConsumer consumer = buffer.getBuffer(
            transparent ?
                HologramRendererRealistic.getHologramTranslucent() :
                HologramRendererRealistic.getHologramSolid()
        );

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (GreedyMesher.MergedQuad quad : mesh) {
            quad.render(consumer, matrix, normal);
        }

        poseStack.popPose();
    }
}
