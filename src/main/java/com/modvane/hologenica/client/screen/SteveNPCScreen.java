package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.menu.SteveNPCMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// GUI screen for Steve NPC with follow toggle
public class SteveNPCScreen extends AbstractContainerScreen<SteveNPCMenu> {
    
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;

    public SteveNPCScreen(SteveNPCMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 80;
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = leftPos + (imageWidth / 2);
        int y = topPos + 30;
        
        // Follow toggle button (centered)
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.toggle_follow"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0)
        ).bounds(centerX - (BUTTON_WIDTH / 2), y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw background
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xC0101010);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw title centered
        Component title = Component.translatable("entity.hologenica.steve_npc");
        int titleWidth = font.width(title);
        graphics.drawString(this.font, title, (imageWidth - titleWidth) / 2, PADDING, 0xFFFFFF, false);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

