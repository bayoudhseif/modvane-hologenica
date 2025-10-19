package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.CentrifugeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

// Client-side GUI screen for Centrifuge
public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(HologenicaMod.MODID, "textures/gui/centrifuge.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Draw background
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Draw progress bar if processing
        if (menu.getBlockEntity() != null) {
            int progress = menu.getBlockEntity().getProcessingTime();
            int duration = menu.getBlockEntity().getProcessingDuration();
            
            if (progress > 0 && duration > 0) {
                int progressWidth = (int) (24.0f * progress / duration);
                graphics.blit(TEXTURE, x + 76, y + 55, 176, 0, progressWidth, 17);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}

