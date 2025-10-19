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
        
        // Get the entity type from the block entity
        String entityTypeString = blockEntity.getEntityType();
        if (entityTypeString == null || entityTypeString.isEmpty()) {
            return;
        }
        
        // Render if cloning (growing) or if ragdoll is complete
        boolean isCloning = blockEntity.isCloning();
        boolean hasRagdoll = blockEntity.hasRagdoll();
        
        if (!isCloning && !hasRagdoll) {
            return;
        }
        
        // Get cloning progress (0.0 to 1.0)
        float progress = blockEntity.getCloningProgress();

        // Parse entity type and create a temporary entity for rendering
        try {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeString);
            EntityType<?> type = EntityType.byString(entityId.toString()).orElse(null);
            
            if (type != null && blockEntity.getLevel() != null) {
                // Create a temporary entity just for rendering (not added to world)
                Entity entity = type.create(blockEntity.getLevel());
                
                if (entity != null) {
                    poseStack.pushPose();

                    // Calculate time-based animations (using world time for smooth animation)
                    long worldTime = blockEntity.getLevel().getGameTime();
                    float time = (worldTime + partialTick) / 20.0f; // Convert to seconds
                    
                    // Bobbing motion (gentle up and down) - only when complete
                    // Use (sin + 1) / 2 to make it oscillate between 0 and 1 (only upward from base)
                    float bobOffset = 0.0f;
                    if (hasRagdoll && !isCloning) {
                        bobOffset = ((float) Math.sin(time * 2.0) + 1.0f) / 2.0f * 0.06f; // Oscillates 0 to 0.06 blocks up
                    }
                    
                    // Position the entity at 9 pixels (0.5625 blocks) from the bottom + bobbing
                    poseStack.translate(0.5, 0.5625 + bobOffset, 0.5);
                    
                    // Rotation when complete
                    float rotationAngle = 180.0f;
                    if (hasRagdoll && !isCloning) {
                        rotationAngle += (time * 30.0f) % 360.0f; // 30 degrees per second, full rotation every 12 seconds
                    }
                    
                    // Rotate to face forward (+ spin when complete)
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
                    
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
                    
                    // If cloning (not complete), scale the entity based on progress
                    // Start at 5% and grow to 100%
                    float growthScale = isCloning ? (0.05f + (progress * 0.95f)) : 1.0f;
                    
                    poseStack.scale(scale * growthScale, scale * growthScale, scale * growthScale);

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


