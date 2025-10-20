package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.menu.HologramMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// Hologram projector GUI with simple buttons
public class HologramScreen extends BaseModScreen<HologramMenu> {

    public HologramScreen(HologramMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int calculateGuiHeight() {
        // Section 1: Display Options (2 rows of buttons)
        // Section 2: Scan Area (1 row of buttons)
        // Section 3: Hologram Size (1 row of buttons)
        return (2 * PADDING) 
            + calculateSectionHeight(2, true)   // Section 1 with gap
            + calculateSectionHeight(1, true)   // Section 2 with gap
            + calculateSectionHeight(1, false); // Section 3 no gap
    }

    @Override
    protected void init() {
        super.init();
        
        int y = topPos + PADDING;
        
        // Section 1: Display Options (3 buttons in 2 rows)
        y += FONT_HEIGHT + LABEL_TO_BUTTON;
        int twoButtonWidth = getButtonWidth(2);
        
        // Row 1: Transparency and Rotation
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.label_transparency"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0)
        ).bounds(leftPos + PADDING, y, twoButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.label_rotation"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1)
        ).bounds(leftPos + PADDING + twoButtonWidth + BUTTON_SPACING, y, twoButtonWidth, BUTTON_HEIGHT).build());
        
        y += BUTTON_HEIGHT + BUTTON_SPACING;
        
        // Row 2: Style toggle (full width)
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.label_style"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2)
        ).bounds(leftPos + PADDING, y, getContentWidth(), BUTTON_HEIGHT).build());
        
        y += BUTTON_HEIGHT + SECTION_GAP;
        
        // Section 2: Scan Area (3 buttons)
        y += FONT_HEIGHT + LABEL_TO_BUTTON;
        int threeButtonWidth = getButtonWidth(3);
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_32"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3)
        ).bounds(leftPos + PADDING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_64"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 4)
        ).bounds(leftPos + PADDING + threeButtonWidth + BUTTON_SPACING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.scan_128"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 5)
        ).bounds(leftPos + PADDING + (2 * threeButtonWidth) + (2 * BUTTON_SPACING), y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        y += BUTTON_HEIGHT + SECTION_GAP;
        
        // Section 3: Hologram Size (3 buttons)
        y += FONT_HEIGHT + LABEL_TO_BUTTON;
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_1"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 6)
        ).bounds(leftPos + PADDING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_3"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 7)
        ).bounds(leftPos + PADDING + threeButtonWidth + BUTTON_SPACING, y, threeButtonWidth, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.size_9"), 
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 8)
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
        y += calculateSectionHeight(2, true); // 2 rows of buttons with gap
        
        graphics.drawString(this.font, Component.translatable("gui.hologenica.scan_area"), PADDING, y, 0xFFFFFF, false);
        y += calculateSectionHeight(1, true); // 1 row with gap
        
        graphics.drawString(this.font, Component.translatable("gui.hologenica.display_size"), PADDING, y, 0xFFFFFF, false);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
