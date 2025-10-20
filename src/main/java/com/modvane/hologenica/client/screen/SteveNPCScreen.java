package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.client.gui.ToggleButton;
import com.modvane.hologenica.menu.SteveNPCMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// GUI screen for Steve NPC with follow toggle
public class SteveNPCScreen extends BaseModScreen<SteveNPCMenu> {

    private static final int BUTTON_WIDTH = 100;

    public SteveNPCScreen(SteveNPCMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int calculateGuiHeight() {
        // Minimal height: just enough for button with padding
        return (2 * PADDING) + BUTTON_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Center button both horizontally and vertically
        int centerX = leftPos + (imageWidth / 2);
        int centerY = topPos + (imageHeight / 2);

        // Follow toggle button with custom rendering (green when following, red when not)
        addRenderableWidget(new ToggleButton(
            centerX - (BUTTON_WIDTH / 2),
            centerY - (BUTTON_HEIGHT / 2),
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            Component.translatable("gui.hologenica.toggle_follow"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0),
            () -> menu.isFollowing() // Use menu's synced data instead of entity directly
        ));
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
        // No labels needed
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

