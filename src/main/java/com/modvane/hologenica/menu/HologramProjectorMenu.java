package com.modvane.hologenica.menu;

import com.modvane.hologenica.block.entity.HologramProjectorBlockEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

// Simple menu for the hologram projector block GUI
// Handles button clicks from the client screen
public class HologramProjectorMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final HologramProjectorBlockEntity blockEntity;

    public HologramProjectorMenu(int containerId, Inventory playerInventory, HologramProjectorBlockEntity blockEntity) {
        super(HologenicaMenus.HOLOGRAM_PROJECTOR.get(), containerId);
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
                blockEntity.toggleTransparency();
                return true;
            } else if (buttonId == 1) {
                blockEntity.toggleRotation();
                return true;
            } else if (buttonId == 2) {
                blockEntity.cycleStyle();
                return true;
            } else if (buttonId == 3) {
                blockEntity.setScanSize(32);
                return true;
            } else if (buttonId == 4) {
                blockEntity.setScanSize(64);
                return true;
            } else if (buttonId == 5) {
                blockEntity.setScanSize(128);
                return true;
            } else if (buttonId == 6) {
                blockEntity.setBlockSize(1);
                return true;
            } else if (buttonId == 7) {
                blockEntity.setBlockSize(3);
                return true;
            } else if (buttonId == 8) {
                blockEntity.setBlockSize(9);
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
    public HologramProjectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Required by AbstractContainerMenu but not used for this simple GUI
    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
