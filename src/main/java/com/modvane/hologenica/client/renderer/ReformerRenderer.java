package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.block.entity.ReformerBlockEntity;
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
public class ReformerRenderer implements BlockEntityRenderer<ReformerBlockEntity> {

    private final EntityRenderDispatcher entityRenderer;
    
    // Cache entities to avoid creating them every frame
    private final java.util.Map<String, Entity> entityCache = new java.util.HashMap<>();

    public ReformerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(ReformerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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
                // Get or create cached entity
                Entity entity = entityCache.get(entityTypeString);
                if (entity == null) {
                    entity = type.create(blockEntity.getLevel());
                    if (entity != null) {
                        entityCache.put(entityTypeString, entity);
                    }
                }
                
                if (entity != null) {
                    // If this is a SteveNPCEntity, set the player UUID for correct skin rendering
                    if (entity instanceof com.modvane.hologenica.entity.SteveNPCEntity steveNPC) {
                        java.util.UUID playerUUID = blockEntity.getPlayerUUID();
                        if (playerUUID != null) {
                            steveNPC.setPlayerUUID(playerUUID);
                        }
                    }
                    
                    poseStack.pushPose();

                    // Position the entity at 8 pixels (0.5 blocks) from the bottom - standing on reformer
                    poseStack.translate(0.5, 0.5, 0.5);
                    
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

