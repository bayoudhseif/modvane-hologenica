package com.modvane.hologenica.client.renderer;

import com.modvane.hologenica.entity.SteveNPCEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// Renderer for Steve NPC - uses the standard humanoid model with Steve skin
public class SteveNPCRenderer extends MobRenderer<SteveNPCEntity, HumanoidModel<SteveNPCEntity>> {

    // Use the default Steve skin
    private static final ResourceLocation STEVE_SKIN = 
        ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public SteveNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SteveNPCEntity entity) {
        return STEVE_SKIN;
    }
    
    @Override
    protected void scale(SteveNPCEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        // Scale to 0.9375F to match exact player size (15/16 of a block)
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}

