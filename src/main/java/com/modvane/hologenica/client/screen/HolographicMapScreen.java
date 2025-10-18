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
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        ).bounds(leftPos + 30, topPos + 30, 116, 20).build());
        
        // Add rotation toggle button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.toggle_rotation"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        ).bounds(leftPos + 30, topPos + 60, 116, 20).build());
        
        // Add scan size buttons
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_size_16"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            }
        ).bounds(leftPos + 30, topPos + 90, 38, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_size_32"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3);
            }
        ).bounds(leftPos + 70, topPos + 90, 38, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_size_64"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 4);
            }
        ).bounds(leftPos + 110, topPos + 90, 38, 20).build());
        
        // Add block size buttons (1x1, 3x3, 9x9)
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.block_size_1"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 5);
            }
        ).bounds(leftPos + 30, topPos + 120, 38, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.block_size_3"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 6);
            }
        ).bounds(leftPos + 70, topPos + 120, 38, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.block_size_9"), 
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 7);
            }
        ).bounds(leftPos + 110, topPos + 120, 38, 20).build());
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
