package com.modvane.hologenica.client.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base class for all Hologenica screens with automatic dynamic sizing.
 * Calculates GUI dimensions based on content instead of hardcoding values.
 */
public abstract class BaseModScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    
    // Standard spacing constants used across all GUIs
    protected static final int PADDING = 10;
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int BUTTON_SPACING = 4;
    protected static final int LABEL_TO_BUTTON = 6;  // Space between label and its buttons
    protected static final int SECTION_GAP = 16;     // Space between sections
    protected static final int FONT_HEIGHT = 9;      // Standard Minecraft font line height
    
    protected static final int DEFAULT_WIDTH = 176;  // Standard GUI width

    public BaseModScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = getGuiWidth();
        this.imageHeight = calculateGuiHeight();
    }

    /**
     * Override this to specify custom GUI width.
     * Default is 176 (standard GUI width).
     */
    protected int getGuiWidth() {
        return DEFAULT_WIDTH;
    }

    /**
     * Override this to calculate the required height for your GUI.
     * Should account for all sections, labels, buttons, and spacing.
     */
    protected abstract int calculateGuiHeight();

    /**
     * Helper method to calculate section height.
     * @param buttonRows Number of button rows in this section
     * @param hasGapAfter Whether this section has a gap after it
     */
    protected int calculateSectionHeight(int buttonRows, boolean hasGapAfter) {
        int height = FONT_HEIGHT + LABEL_TO_BUTTON + (BUTTON_HEIGHT * buttonRows);
        if (buttonRows > 1) {
            height += BUTTON_SPACING * (buttonRows - 1);
        }
        if (hasGapAfter) {
            height += SECTION_GAP;
        }
        return height;
    }

    /**
     * Helper to get content width (accounting for padding on both sides).
     */
    protected int getContentWidth() {
        return imageWidth - (2 * PADDING);
    }

    /**
     * Helper to calculate button width for N buttons in a row.
     */
    protected int getButtonWidth(int buttonsInRow) {
        return (getContentWidth() - (BUTTON_SPACING * (buttonsInRow - 1))) / buttonsInRow;
    }
}

