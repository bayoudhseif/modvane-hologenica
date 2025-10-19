package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.CloningPodBlockEntity;
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

// Renders a static ragdoll model inside the cloning pod when complete
public class CloningPodRenderer implements BlockEntityRenderer<CloningPodBlockEntity> {

    private final EntityRenderDispatcher entityRenderer;

    public CloningPodRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(CloningPodBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        
        // Only render if there's a ragdoll to display
        if (!blockEntity.hasRagdoll()) {
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
            EntityType<?> type = EntityType.byString(entityId.toString()).orElse(null);
            
            if (type != null && blockEntity.getLevel() != null) {
                // Create a temporary entity just for rendering (not added to world)
                Entity entity = type.create(blockEntity.getLevel());
                
                if (entity != null) {
                    poseStack.pushPose();

                    // Position the entity at 9 pixels (0.5625 blocks) from the bottom
                    poseStack.translate(0.5, 0.5625, 0.5);
                    
                    // Rotate to face forward
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    
                    // Get entity dimensions
                    float entityWidth = entity.getBbWidth();
                    float entityHeight = entity.getBbHeight();
                    
                    // Add safety margin for visual models that extend beyond hitbox
                    // (heads, tails, etc. on quadrupeds like pigs, cows, sheep)
                    float visualMargin = 1.3f;
                    float visualWidth = entityWidth * visualMargin;
                    float visualHeight = entityHeight; // Height is usually accurate
                    
                    // Find the largest dimension
                    float maxDimension = Math.max(visualWidth, visualHeight);
                    
                    // Target size to fit in chamber (0.85 blocks to leave some margin)
                    float targetSize = 0.85f;
                    
                    // Only scale down if too large, never scale up
                    float scale;
                    if (maxDimension > targetSize) {
                        scale = targetSize / maxDimension;
                    } else {
                        scale = 1.0f; // Keep original size if it fits
                    }
                    
                    // Safety clamp for extremely large entities
                    scale = Math.max(0.1f, scale);
                    
                    poseStack.scale(scale, scale, scale);

                    // Set entity to silent (no sound effects)
                    entity.setSilent(true);

                    // Render the entity as a static ragdoll with full brightness
                    entityRenderer.render(entity, 0, 0, 0, 0, partialTick, poseStack, buffer, 0xF000F0);

                    poseStack.popPose();
                }
            }
        } catch (Exception e) {
            // Failed to render entity - silently fail
        }
    }
}

