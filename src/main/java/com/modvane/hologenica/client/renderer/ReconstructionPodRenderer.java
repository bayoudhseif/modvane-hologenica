package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.ReconstructionPodBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

// Renders a growing entity inside the reconstruction pod (1% to 100% scale)
public class ReconstructionPodRenderer implements BlockEntityRenderer<ReconstructionPodBlockEntity> {

    private final EntityRenderDispatcher entityRenderer;

    public ReconstructionPodRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(ReconstructionPodBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        
        // Only render if reconstructing
        if (!blockEntity.isReconstructing()) {
            return;
        }

        // Get the entity type from the block entity
        String entityTypeString = blockEntity.getEntityType();
        if (entityTypeString == null || entityTypeString.isEmpty()) {
            return;
        }

        // Parse entity type and create a temporary entity for rendering
        try {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeString);
            EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(entityId);
            
            if (type != null && blockEntity.getLevel() != null) {
                // Create a temporary entity just for rendering (not added to world)
                Entity entity = type.create(blockEntity.getLevel());
                
                if (entity != null) {
                    poseStack.pushPose();

                    // Position the entity at 9 pixels (0.5625 blocks) from the bottom
                    poseStack.translate(0.5, 0.5625, 0.5);
                    
                    // Rotate to face forward
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    
                    // Apply growth percentage (1% to 100% of real entity size)
                    float growthPercentage = blockEntity.getProgressPercentage();
                    float growthScale = 0.01f + (growthPercentage * 0.99f); // 1% to 100%
                    
                    poseStack.scale(growthScale, growthScale, growthScale);

                    // Set entity to silent (no sound effects)
                    entity.setSilent(true);

                    // Render the entity with full brightness
                    entityRenderer.render(entity, 0, 0, 0, 0, partialTick, poseStack, buffer, 0xF000F0);

                    poseStack.popPose();
                }
            }
        } catch (Exception e) {
            // Failed to render entity - silently fail
        }
    }
}

