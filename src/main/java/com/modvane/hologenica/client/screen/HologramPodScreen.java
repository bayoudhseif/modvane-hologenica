package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.menu.HologramPodMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// Hologram pod GUI with simple buttons
public class HologramPodScreen extends AbstractContainerScreen<HologramPodMenu> {
    
    // Consistent spacing constants
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;
    private static final int LABEL_TO_BUTTON = 6;  // Space between label and its buttons
    private static final int SECTION_GAP = 16;      // Space between sections

    public HologramPodScreen(HologramPodMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 160;
    }

    @Override
    protected void init() {
        super.init();
        
        int y = topPos + PADDING;
        int contentWidth = imageWidth - (2 * PADDING);
        
        // Section 1: Display Options (2 buttons)
        y += font.lineHeight + LABEL_TO_BUTTON;
        int twoButtonWidth = (contentWidth - BUTTON_SPACING) / 2;
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.label_transparency"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0)
        ).bounds(leftPos + PADDING, y, twoButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.label_rotation"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1)
        ).bounds(leftPos + PADDING + twoButtonWidth + BUTTON_SPACING, y, twoButtonWidth, BUTTON_HEIGHT).build());
        
        y += BUTTON_HEIGHT + SECTION_GAP;
        
        // Section 2: Scan Area (3 buttons)
        y += font.lineHeight + LABEL_TO_BUTTON;
        int threeButtonWidth = (contentWidth - (2 * BUTTON_SPACING)) / 3;
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_16"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2)
        ).bounds(leftPos + PADDING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_32"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3)
        ).bounds(leftPos + PADDING + threeButtonWidth + BUTTON_SPACING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_64"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 4)
        ).bounds(leftPos + PADDING + (2 * threeButtonWidth) + (2 * BUTTON_SPACING), y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        y += BUTTON_HEIGHT + SECTION_GAP;
        
        // Section 3: Hologram Size (3 buttons)
        y += font.lineHeight + LABEL_TO_BUTTON;
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_1"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 5)
        ).bounds(leftPos + PADDING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_3"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 6)
        ).bounds(leftPos + PADDING + threeButtonWidth + BUTTON_SPACING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_9"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 7)
        ).bounds(leftPos + PADDING + (2 * threeButtonWidth) + (2 * BUTTON_SPACING), y, threeButtonWidth, BUTTON_HEIGHT).build());
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
        int y = PADDING;
        
        // Section headers (each section follows the pattern: label -> buttons -> gap)
        graphics.drawString(this.font, Component.translatable("gui.hologenica.display_options"), PADDING, y, 0xFFFFFF, false);
        y += font.lineHeight + LABEL_TO_BUTTON + BUTTON_HEIGHT + SECTION_GAP;
        
        graphics.drawString(this.font, Component.translatable("gui.hologenica.scan_area"), PADDING, y, 0xFFFFFF, false);
        y += font.lineHeight + LABEL_TO_BUTTON + BUTTON_HEIGHT + SECTION_GAP;
        
        graphics.drawString(this.font, Component.translatable("gui.hologenica.display_size"), PADDING, y, 0xFFFFFF, false);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
