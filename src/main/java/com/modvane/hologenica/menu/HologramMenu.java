package com.modvane.hologenica.menu;

import com.modvane.hologenica.block.entity.HologramBlockEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;

// Simple menu for the hologram block GUI
// Handles button clicks from the client screen
public class HologramMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final HologramBlockEntity blockEntity;
    private final ContainerData data;

    public HologramMenu(int containerId, Inventory playerInventory, HologramBlockEntity blockEntity) {
        super(HologenicaMenus.HOLOGRAM.get(), containerId);
        if (blockEntity != null) {
            this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
            this.blockEntity = blockEntity;
        } else {
            this.access = ContainerLevelAccess.NULL;
            this.blockEntity = null;
        }
        
        // Create data container to sync toggle states to client
        // Index 0: transparency, Index 1: rotation
        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);
    }

    // Handle button clicks from the client screen
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (blockEntity != null) {
            if (buttonId == 0) {
                blockEntity.toggleTransparency();
                return true;
            } else if (buttonId == 1) {
                blockEntity.toggleRotation();
                return true;
            } else if (buttonId == 2) {
                blockEntity.setScanSize(32);
                return true;
            } else if (buttonId == 3) {
                blockEntity.setScanSize(64);
                return true;
            } else if (buttonId == 4) {
                blockEntity.setScanSize(128);
                return true;
            } else if (buttonId == 5) {
                blockEntity.setBlockSize(1);
                return true;
            } else if (buttonId == 6) {
                blockEntity.setBlockSize(3);
                return true;
            } else if (buttonId == 7) {
                blockEntity.setBlockSize(9);
                return true;
            }
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // Update the synced data with current toggle states
        if (blockEntity != null) {
            this.data.set(0, blockEntity.isTransparentMode() ? 1 : 0);
            this.data.set(1, blockEntity.isRotationEnabled() ? 1 : 0);
        }
    }

    // Get transparency state from synced data (works on both client and server)
    public boolean isTransparentMode() {
        return this.data.get(0) == 1;
    }

    // Get rotation state from synced data (works on both client and server)
    public boolean isRotationEnabled() {
        return this.data.get(1) == 1;
    }

    // Check if the player can still access this menu
    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    // Get the block entity for the screen to access
    public HologramBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Required by AbstractContainerMenu but not used for this simple GUI
    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
