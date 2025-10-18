package com.modvane.hologenica.menu;

import com.modvane.hologenica.block.entity.HolographicMapBlockEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

// Simple menu for the holographic map block GUI
// Handles button clicks from the client screen
public class HolographicMapMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final HolographicMapBlockEntity blockEntity;

    public HolographicMapMenu(int containerId, Inventory playerInventory, HolographicMapBlockEntity blockEntity) {
        super(HologenicaMenus.HOLOGRAPHIC_MAP.get(), containerId);
        if (blockEntity != null) {
            this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
            this.blockEntity = blockEntity;
        } else {
            this.access = ContainerLevelAccess.NULL;
            this.blockEntity = null;
        }
    }

    // Handle button clicks from the client screen
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (blockEntity != null) {
            if (buttonId == 0) {
                // Toggle transparency mode
                blockEntity.toggleTransparency();
                return true;
            } else if (buttonId == 1) {
                // Toggle rotation
                blockEntity.toggleRotation();
                return true;
            }
        }
        return false;
    }

    // Check if the player can still access this menu
    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    // Get the block entity for the screen to access
    public HolographicMapBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Required by AbstractContainerMenu but not used for this simple GUI
    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
