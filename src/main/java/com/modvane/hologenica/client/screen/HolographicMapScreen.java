package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.menu.HolographicMapMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// Super simple GUI screen for the holographic map block
// Just shows a button to toggle transparency
public class HolographicMapScreen extends AbstractContainerScreen<HolographicMapMenu> {

    public HolographicMapScreen(HolographicMapMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        
        // Add transparency toggle button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.toggle_transparency"), 
            button -> {
                // Send button click to server via menu system
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        ).bounds(leftPos + 30, topPos + 50, 116, 20).build());
        
        // Add rotation toggle button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.toggle_rotation"), 
            button -> {
                // Send button click to server via menu system
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        ).bounds(leftPos + 30, topPos + 80, 116, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // No background - just the button
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // No labels - just the button
    }
}
