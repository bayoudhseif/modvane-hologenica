package com.modvane.hologenica.menu;

import com.modvane.hologenica.block.entity.NeurocellBlockEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// Menu for Neurocell GUI
public class NeurocellMenu extends AbstractContainerMenu {
    
    private final Container container;
    private NeurocellBlockEntity blockEntity;

    // Constructor for server-side (with block entity)
    public NeurocellMenu(int containerId, Inventory playerInventory, NeurocellBlockEntity blockEntity) {
        super(HologenicaMenus.NEUROCELL.get(), containerId);
        this.container = blockEntity.getInventory();
        this.blockEntity = blockEntity;

        // Bioscanner slot (center of GUI)
        this.addSlot(new Slot(container, 0, 80, 35));

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // Constructor for client-side (without block entity)
    public NeurocellMenu(int containerId, Inventory playerInventory, Container container) {
        super(HologenicaMenus.NEUROCELL.get(), containerId);
        this.container = container;
        this.blockEntity = null;

        // Bioscanner slot (center of GUI)
        this.addSlot(new Slot(container, 0, 80, 35));

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            
            if (index == 0) {
                // Moving from neurocell to player inventory
                if (!this.moveItemStackTo(slotItem, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to neurocell
                if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public NeurocellBlockEntity getBlockEntity() {
        return blockEntity;
    }
}

