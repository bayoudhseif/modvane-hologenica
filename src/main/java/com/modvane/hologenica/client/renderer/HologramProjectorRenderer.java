package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.HologramProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;

// Renders a 3D holographic projection of terrain above the hologram projector block
// Delegates to either holographic or realistic renderer based on user setting
public class HologramProjectorRenderer implements BlockEntityRenderer<HologramProjectorBlockEntity> {

    public HologramProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HologramProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        if (!blockEntity.hasValidRegion() || blockEntity.getLevel() == null) return;

        Level level = blockEntity.getLevel();
        int minX = blockEntity.getScanMinX();
        int maxX = blockEntity.getScanMaxX();
        int minZ = blockEntity.getScanMinZ();
        int maxZ = blockEntity.getScanMaxZ();

        // Determine which renderer to use based on style
        var style = blockEntity.getRenderStyle();

        // Scan terrain if needed (each renderer may use different scan methods)
        int[][][] terrain = blockEntity.getCachedTerrain();
        if (terrain == null || blockEntity.needsRescan()) {
            terrain = switch (style) {
                case CLASSIC -> HologramRendererClassic.scanTerrain(level, minX, maxX, minZ, maxZ);
                case REALISTIC -> HologramRendererRealistic.scanTerrain(level, minX, maxX, minZ, maxZ);
            };
            if (terrain == null) return;
            blockEntity.setCachedTerrain(terrain);
        }

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

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

        // Delegate to the appropriate renderer
        boolean transparent = blockEntity.isTransparentMode();
        switch (style) {
            case CLASSIC -> HologramRendererClassic.render(poseStack, buffer, terrain, width, depth, transparent);
            case REALISTIC -> HologramRendererRealistic.render(poseStack, buffer, terrain, width, depth, transparent);
        }

        poseStack.popPose();
    }
}
