package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.HologramBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

// Renders a 3D holographic projection of terrain above the hologram projector block
// Uses optimized greedy meshing + VBO caching for maximum performance
public class HologramRenderer implements BlockEntityRenderer<HologramBlockEntity> {

    public HologramRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(HologramBlockEntity blockEntity) {
        return false; // Use Minecraft's native frustum culling
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

        // Get render type
        boolean transparent = blockEntity.isTransparentMode();
        RenderType renderType = transparent ?
            HologramRendererRealistic.getHologramTranslucent() :
            HologramRendererRealistic.getHologramSolid();

        // Build VBO if needed
        HologramMeshCache vboCache = blockEntity.getVBOCache();
        if (vboCache != null && vboCache.needsRebuild()) {
            vboCache.buildMesh(mesh, renderType);
        }

        // Render from VBO
        if (vboCache != null) {
            VertexBuffer vbo = vboCache.getVertexBuffer();
            if (vbo != null) {
                // Setup render state
                renderType.setupRenderState();

                // Get the shader
                ShaderInstance shader = RenderSystem.getShader();
                if (shader != null) {
                    // Set the projection matrix
                    shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());

                    // Combine the ModelView stack with our pose stack
                    RenderSystem.getModelViewStack().pushMatrix();
                    RenderSystem.getModelViewStack().mul(poseStack.last().pose());
                    RenderSystem.applyModelViewMatrix();

                    // Set the model-view matrix to the shader
                    shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());

                    // Apply the shader
                    shader.apply();

                    // Draw the VBO
                    vbo.bind();
                    vbo.draw();

                    // Restore model-view stack
                    RenderSystem.getModelViewStack().popMatrix();
                    RenderSystem.applyModelViewMatrix();
                }

                VertexBuffer.unbind();
                renderType.clearRenderState();
            }
        }

        poseStack.popPose();
    }
}
