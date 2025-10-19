package com.modvane.hologenica.client.screen;

import com.modvane.hologenica.menu.TelepadMenu;
import com.modvane.hologenica.network.SetTelepadNamePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

// Telepad GUI with text input for naming
public class TelepadScreen extends AbstractContainerScreen<TelepadMenu> {

    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXTFIELD_HEIGHT = 20;
    private static final int SPACING = 6;

    private EditBox nameField;

    public TelepadScreen(TelepadMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 100;
    }

    @Override
    protected void init() {
        super.init();

        int y = topPos + PADDING;
        int contentWidth = imageWidth - (2 * PADDING);

        // Label is drawn in renderLabels(), so skip space for it
        y += font.lineHeight + SPACING;

        // Text input field for telepad name
        nameField = new EditBox(
            font,
            leftPos + PADDING,
            y,
            contentWidth,
            TEXTFIELD_HEIGHT,
            Component.translatable("gui.hologenica.telepad_name")
        );
        nameField.setMaxLength(32);

        String currentName = menu.getBlockEntity() != null ? menu.getBlockEntity().getTelepadName() : "";
        nameField.setValue(currentName);

        // Set focus to the text field so you can immediately start typing
        setInitialFocus(nameField);

        addRenderableWidget(nameField);

        y += TEXTFIELD_HEIGHT + SPACING;

        // Save button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.hologenica.save"),
            button -> saveNameToServer()
        ).bounds(leftPos + PADDING, y, contentWidth, BUTTON_HEIGHT).build());
    }

    // Send the telepad name to the server
    private void saveNameToServer() {
        String name = nameField.getValue();
        PacketDistributor.sendToServer(new SetTelepadNamePacket(menu.getPos(), name));

        // Close the GUI after sending packet
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw background
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw title
        graphics.drawString(this.font, Component.translatable("gui.hologenica.telepad_name_label"), PADDING, PADDING, 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Prevent keybinds from triggering while typing
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If the text field is focused, handle the key in the text field first
        if (nameField != null && nameField.isFocused()) {
            if (nameField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            // Don't let other keybinds process when text field is focused
            // except for ESC key to close the GUI
            if (keyCode != 256) { // 256 is ESC
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // If text field is focused, let it handle character input
        if (nameField != null && nameField.isFocused()) {
            return nameField.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String currentValue = nameField.getValue();
        super.resize(minecraft, width, height);
        nameField.setValue(currentValue);
    }
}
