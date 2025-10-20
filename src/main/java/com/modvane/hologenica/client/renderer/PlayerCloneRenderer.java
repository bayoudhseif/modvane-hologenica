package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.entity.PlayerCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// Renderer for Player Clone - uses the actual player's skin and model (Steve or Alex)
public class PlayerCloneRenderer extends MobRenderer<PlayerCloneEntity, HumanoidModel<PlayerCloneEntity>> {

    // Default Steve skin as fallback
    private static final ResourceLocation STEVE_SKIN = 
        ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    
    // Hold both Steve (wide) and Alex (slim) models
    private final HumanoidModel<PlayerCloneEntity> steveModel;
    private final HumanoidModel<PlayerCloneEntity> alexModel;

    public PlayerCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.steveModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)); // Wide arms
        this.alexModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)); // Slim arms
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerCloneEntity entity) {
        java.util.UUID playerUUID = entity.getPlayerUUID();
        
        // Try to get the actual player's skin using their UUID
        if (playerUUID != null) {
            var connection = Minecraft.getInstance().getConnection();
            
            if (connection != null) {
                PlayerInfo playerInfo = connection.getPlayerInfo(playerUUID);
                
                if (playerInfo != null) {
                    // Switch model based on player skin type
                    if (playerInfo.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM) {
                        this.model = alexModel;
                    } else {
                        this.model = steveModel;
                    }
                    
                    return playerInfo.getSkin().texture();
                }
            }
        }
        
        // Fallback to default Steve skin and model
        this.model = steveModel;
        return STEVE_SKIN;
    }
    
    @Override
    protected void scale(PlayerCloneEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        // Scale to 0.9375F to match exact player size (15/16 of a block)
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}

