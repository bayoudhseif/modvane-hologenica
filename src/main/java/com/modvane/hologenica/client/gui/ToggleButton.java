package com.modvane.hologenica.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

// Button that changes its text based on toggle state
public class ToggleButton extends Button {

    private final Component baseMessage;
    private final Supplier<Boolean> toggleStateSupplier;

    public ToggleButton(int x, int y, int width, int height, Component message,
                        OnPress onPress, Supplier<Boolean> toggleStateSupplier) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.baseMessage = message;
        this.toggleStateSupplier = toggleStateSupplier;
    }

    @Override
    public Component getMessage() {
        boolean isOn = toggleStateSupplier.get();
        String symbol = isOn ? "✔" : "✖";
        return Component.literal(symbol + " " + baseMessage.getString());
    }
}
